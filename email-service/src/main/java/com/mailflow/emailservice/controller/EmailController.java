package com.mailflow.emailservice.controller;

import com.mailflow.emailservice.dto.email.EmailResponse;
import com.mailflow.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
public class EmailController {

  private final EmailService emailService;

  @GetMapping("/{id}")
  public Mono<EmailResponse> getEmail(@PathVariable Long id) {
    return emailService.getEmail(id);
  }

  @GetMapping("/recent")
  public Flux<EmailResponse> getRecentEmails(@RequestParam(defaultValue = "10") int limit) {
    return emailService.getRecentEmails(limit);
  }

  @PostMapping("/campaign/{campaignId}/contact/{contactId}/template/{templateId}")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<EmailResponse> sendEmail(
      @PathVariable Long campaignId, @PathVariable Long contactId, @PathVariable Long templateId) {
    return emailService.sendEmail(campaignId, contactId, templateId);
  }

  @PostMapping("/retry")
  public Flux<EmailResponse> retryFailedEmails(@RequestParam(defaultValue = "60") int minutes) {
    return emailService.retryFailedEmails(minutes);
  }

  @GetMapping("/stats")
  public Mono<Map<String, Object>> getEmailStats() {
    return emailService.getEmailStatsSummary();
  }

  @GetMapping("/analytics")
  public Mono<Map<String, Object>> getEmailAnalytics(
          @RequestParam(defaultValue = "year") String period) {
    return emailService.getEmailAnalytics(period);
  }
}
