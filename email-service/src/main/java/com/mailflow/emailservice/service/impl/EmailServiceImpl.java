package com.mailflow.emailservice.service.impl;

import com.mailflow.emailservice.client.ContactServiceClient;
import com.mailflow.emailservice.client.TemplateServiceClient;
import com.mailflow.emailservice.domain.Email;
import com.mailflow.emailservice.domain.EmailStatus;
import com.mailflow.emailservice.dto.campaign.CampaignTriggeredEvent;
import com.mailflow.emailservice.dto.contact.ContactDTO;
import com.mailflow.emailservice.dto.email.EmailAnalyticsDTO;
import com.mailflow.emailservice.dto.email.EmailResponse;
import com.mailflow.emailservice.dto.email.EmailSentEvent;
import com.mailflow.emailservice.kafka.KafkaEventPublisher;
import com.mailflow.emailservice.mapper.EmailMapper;
import com.mailflow.emailservice.repository.EmailRepository;
import com.mailflow.emailservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

  @Value("${base.url}")
  private String baseUrl;

  private final EmailRepository emailRepository;
  private final ContactServiceClient contactServiceClient;
  private final TemplateServiceClient templateServiceClient;
  private final JavaMailSender mailSender;
  private final KafkaEventPublisher eventPublisher;
  private final EmailMapper emailMapper;

  @Override
  public Mono<EmailResponse> sendEmail(Long campaignId, Long contactId, Long templateId) {

    String trackingId = UUID.randomUUID().toString();

    log.info(
        "Preparing to send email: campaignId={}, contactId={}, templateId={}",
        campaignId,
        contactId,
        templateId);

    return executeBlocking(() -> contactServiceClient.getContact(contactId))
        .onErrorResume(
            e -> {
              log.error("Failed to fetch contact: {}", e.getMessage());
              return createFailedEmail(
                      campaignId,
                      contactId,
                      templateId,
                      null,
                      "Failed to fetch contact: " + e.getMessage())
                  .flatMap(response -> Mono.empty());
            })
        .flatMap(
            contact -> {
              if (contact == null || contact.email() == null) {
                return createFailedEmail(
                    campaignId, contactId, templateId, null, "Contact or contact email is null");
              }

              // Get template information
              return executeBlocking(() -> templateServiceClient.getTemplate(templateId))
                  .onErrorResume(
                      e -> {
                        log.error("Failed to fetch template: {}", e.getMessage());
                        return createFailedEmail(
                                campaignId,
                                contactId,
                                templateId,
                                contact.email(),
                                "Failed to fetch template: " + e.getMessage())
                            .flatMap(response -> Mono.empty());
                      })
                  .flatMap(
                      template -> {
                        if (template == null) {
                          return createFailedEmail(
                              campaignId,
                              contactId,
                              templateId,
                              contact.email(),
                              "Template is null");
                        }

                        // Prepare variables for template rendering
                        Map<String, String> variables = prepareTemplateVariables(contact);

                        // Render the template
                        return executeBlocking(
                                () -> templateServiceClient.renderTemplate(templateId, variables))
                            .onErrorResume(
                                e -> {
                                  log.error("Failed to render template: {}", e.getMessage());
                                  return createFailedEmail(
                                          campaignId,
                                          contactId,
                                          templateId,
                                          contact.email(),
                                          "Failed to render template: " + e.getMessage())
                                      .flatMap(response -> Mono.empty());
                                })
                            .flatMap(
                                renderedTemplate -> {
                                  if (renderedTemplate == null) {
                                    return createFailedEmail(
                                        campaignId,
                                        contactId,
                                        templateId,
                                        contact.email(),
                                        "Rendered template is null");
                                  }

                                  String subject = renderedTemplate.get("subject");
                                  String content = renderedTemplate.get("content");

                                  // Add tracking pixel to content
                                  content = addTrackingPixel(content, trackingId);

                                  Email email =
                                      Email.builder()
                                          .campaignId(campaignId)
                                          .contactId(contactId)
                                          .templateId(templateId)
                                          .recipientEmail(contact.email())
                                          .subject(subject)
                                          .content(content)
                                          .status(EmailStatus.PENDING)
                                          .trackingId(trackingId)
                                          .build();

                                  // Save email in database
                                  return emailRepository
                                      .save(email)
                                      .flatMap(
                                          savedEmail -> {
                                            // Send email asynchronously
                                            return Mono.fromCallable(
                                                    () -> {
                                                      try {
                                                        sendMimeMessage(savedEmail);
                                                        savedEmail.setStatus(EmailStatus.SENT);
                                                        savedEmail.setSentAt(LocalDateTime.now());
                                                        return savedEmail;
                                                      } catch (Exception e) {
                                                        log.error(
                                                            "Failed to send email: {}",
                                                            e.getMessage(),
                                                            e);
                                                        savedEmail.setStatus(EmailStatus.FAILED);
                                                        savedEmail.setErrorMessage(e.getMessage());
                                                        return savedEmail;
                                                      }
                                                    })
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .flatMap(emailRepository::save)
                                                .doOnNext(
                                                    sentEmail -> {
                                                      if (sentEmail.getStatus()
                                                          == EmailStatus.SENT) {
                                                        // Publish email sent event
                                                        EmailSentEvent sentEvent =
                                                            EmailSentEvent.builder()
                                                                .emailId(sentEmail.getId())
                                                                .campaignId(
                                                                    sentEmail.getCampaignId())
                                                                .contactId(sentEmail.getContactId())
                                                                .recipientEmail(
                                                                    sentEmail.getRecipientEmail())
                                                                .trackingId(
                                                                    sentEmail.getTrackingId())
                                                                .sentAt(sentEmail.getSentAt())
                                                                .build();

                                                        eventPublisher
                                                            .publishEmailSentEvent(sentEvent)
                                                            .subscribe(
                                                                null,
                                                                error ->
                                                                    log.error(
                                                                        "Failed to publish email sent event: {}",
                                                                        error.getMessage(),
                                                                        error));
                                                      }
                                                    })
                                                .map(emailMapper::toResponse);
                                          });
                                });
                      });
            });
  }

  @Override
  public void processTriggeredCampaign(CampaignTriggeredEvent event) {
    log.info(
        "Processing triggered campaign: campaignId={}, contactId={}, templateId={}",
        event.campaignId(),
        event.contactId(),
        event.templateId());

    sendEmail(event.campaignId(), event.contactId(), event.templateId())
        .subscribe(
            emailResponse -> log.info("Email sent successfully: {}", emailResponse),
            error -> log.error("Failed to send email: {}", error.getMessage()));
  }

  @Override
  public Mono<EmailResponse> getEmail(Long id) {
    return emailRepository.findById(id).map(emailMapper::toResponse);
  }

  @Override
  public Flux<EmailResponse> getRecentEmails(int limit) {
    return emailRepository.findRecentEmails(limit).map(emailMapper::toResponse);
  }

  @Override
  public Flux<EmailResponse> retryFailedEmails(int minutes) {
    LocalDateTime timeout = LocalDateTime.now().minusMinutes(minutes);

    return emailRepository
        .findByStatusAndTimeout(EmailStatus.FAILED, timeout)
        .flatMap(
            email -> {
              log.info("Retrying failed email: {}", email.getId());
              return sendEmail(email.getCampaignId(), email.getContactId(), email.getTemplateId());
            });
  }

    @Override
    public Mono<Map<String, Object>> getEmailAnalytics(String period) {
        LocalDateTime startDate = getStartDateForPeriod(period);

        return emailRepository
                .getEmailAnalytics(startDate)
                .collectList()
                .map(
                        results -> {
                            List<String> labels = new ArrayList<>();
                            List<Long> sent = new ArrayList<>();
                            List<Long> opened = new ArrayList<>();
                            List<Long> clicked = new ArrayList<>();

                            for (EmailAnalyticsDTO result : results) {
                                String monthName = getMonthName(result.month());
                                labels.add(monthName);
                                sent.add(result.sent() != null ? result.sent() : 0L);
                                opened.add(result.opened() != null ? result.opened() : 0L);
                                clicked.add(result.clicked() != null ? result.clicked() : 0L);
                            }

                            Map<String, Object> response = new HashMap<>();
                            response.put("labels", labels);
                            response.put("sent", sent);
                            response.put("opened", opened);
                            response.put("clicked", clicked);

                            return response;
                        });
    }

    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "January";
            case 2 -> "February";
            case 3 -> "March";
            case 4 -> "April";
            case 5 -> "May";
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> "Unknown";
        };
    }

  @Override
  public Mono<Map<String, Object>> getEmailStatsSummary() {
    LocalDateTime lastMonthStart =
        LocalDateTime.now()
            .minusMonths(1)
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0);
    LocalDateTime thisMonthStart =
        LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

    // Current month stats
    Mono<Long> totalSent =
        emailRepository.countByStatusAndCreatedAtAfter(EmailStatus.SENT, thisMonthStart);
    Mono<Long> totalOpened =
        emailRepository.countByStatusAndCreatedAtAfter(EmailStatus.OPENED, thisMonthStart);
    Mono<Long> totalClicked =
        emailRepository.countByStatusAndCreatedAtAfter(EmailStatus.CLICKED, thisMonthStart);

    // Previous month stats
    Mono<Long> lastMonthSent =
        emailRepository.countByStatusAndCreatedAtBetween(
            EmailStatus.SENT, lastMonthStart, thisMonthStart);
    Mono<Long> lastMonthOpened =
        emailRepository.countByStatusAndCreatedAtBetween(
            EmailStatus.OPENED, lastMonthStart, thisMonthStart);
    Mono<Long> lastMonthClicked =
        emailRepository.countByStatusAndCreatedAtBetween(
            EmailStatus.CLICKED, lastMonthStart, thisMonthStart);

    return Mono.zip(
            totalSent, totalOpened, totalClicked, lastMonthSent, lastMonthOpened, lastMonthClicked)
        .map(
            tuple -> {
              long sent = tuple.getT1();
              long opened = tuple.getT2();
              long clicked = tuple.getT3();
              long prevSent = tuple.getT4();
              long prevOpened = tuple.getT5();
              long prevClicked = tuple.getT6();

              double openRate = sent > 0 ? (double) opened / sent * 100 : 0;
              double prevOpenRate = prevSent > 0 ? (double) prevOpened / prevSent * 100 : 0;

              // Calculate percentage changes
              double sentChange = calculatePercentageChange(sent, prevSent);
              double openedChange = calculatePercentageChange(opened, prevOpened);
              double clickedChange = calculatePercentageChange(clicked, prevClicked);
              double openRateChange = openRate - prevOpenRate;

              Map<String, Object> stats = new HashMap<>();
              stats.put("totalSent", sent);
              stats.put("totalOpened", opened);
              stats.put("totalClicked", clicked);
              stats.put("openRate", Math.round(openRate));
              stats.put("sentChangePercentage", sentChange);
              stats.put("openedChangePercentage", openedChange);
              stats.put("clickedChangePercentage", clickedChange);
              stats.put("openRateChangePercentage", openRateChange);

              return stats;
            });
  }

  private double calculatePercentageChange(long current, long previous) {
    if (previous == 0) return 100.0; // If previous was 0, consider it 100% increase
    return ((double) (current - previous) / previous) * 100;
  }

  private LocalDateTime getStartDateForPeriod(String period) {
    LocalDateTime now = LocalDateTime.now();
    return switch (period.toLowerCase()) {
      case "week" -> now.minusWeeks(1);
      case "month" -> now.minusMonths(1);
      case "quarter" -> now.minusMonths(3);
      default -> now.minusYears(1);
    };
  }

  private Mono<EmailResponse> createFailedEmail(
      Long campaignId,
      Long contactId,
      Long templateId,
      String recipientEmail,
      String errorMessage) {

    Email failedEmail =
        Email.builder()
            .campaignId(campaignId)
            .contactId(contactId)
            .templateId(templateId)
            .recipientEmail(recipientEmail)
            .status(EmailStatus.FAILED)
            .errorMessage(errorMessage)
            .build();

    return emailRepository.save(failedEmail).map(emailMapper::toResponse);
  }

  private <T> Mono<T> executeBlocking(Callable<T> callable) {
    return Mono.fromCallable(callable).subscribeOn(Schedulers.boundedElastic());
  }

  private Map<String, String> prepareTemplateVariables(ContactDTO contact) {
    Map<String, String> variables = new HashMap<>();

    variables.put("email", contact.email());
    variables.put("firstName", contact.firstName());
    variables.put("lastName", contact.lastName());
    variables.put("fullName", contact.firstName() + " " + contact.lastName());

    return variables;
  }

  private String addTrackingPixel(String content, String trackingId) {
    String trackingPixel =
        "<img src=\"" + baseUrl + "/t/" + trackingId + "\" width=\"1\" height=\"1\" alt=\"\" />";

    // Add tracking pixel before the closing body tag
    if (content.contains("</body>")) {
      return content.replace("</body>", trackingPixel + "</body>");
    } else {
      return content + trackingPixel;
    }
  }

  private void sendMimeMessage(Email email) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(email.getRecipientEmail());
    helper.setSubject(email.getSubject());
    helper.setText(email.getContent(), true);

    mailSender.send(message);
    log.info("Email sent to: {}", email.getRecipientEmail());
  }
}
