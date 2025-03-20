package com.mailflow.emailservice.kafka;

import com.mailflow.emailservice.dto.campaign.CampaignTriggeredEvent;
import com.mailflow.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventListener {

  private final EmailService emailService;

  @KafkaListener(topics = "campaign-triggered", groupId = "email-service-group")
  public void handleCampaignTriggeredEvent(CampaignTriggeredEvent event) {
    log.info("Received campaign triggered event: {}", event);

    // Process the event reactively and subscribe to ensure execution
    Mono.fromRunnable(() -> emailService.processTriggeredCampaign(event))
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe(
            null,
            error ->
                log.error(
                    "Error processing campaign triggered event: {}", error.getMessage(), error));
  }
}
