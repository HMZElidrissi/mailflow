package com.mailflow.emailservice.kafka;

import com.mailflow.emailservice.dto.email.EmailSentEvent;
import com.mailflow.emailservice.dto.email.EmailStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

  private static final String EMAIL_EVENTS_TOPIC = "email-events";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public Mono<Void> publishEmailSentEvent(EmailSentEvent event) {
    return Mono.fromCallable(
            () -> {
              log.info("Publishing email sent event: {}", event);
              return kafkaTemplate.send(EMAIL_EVENTS_TOPIC, "email-sent", event);
            })
        .then();
  }

  public Mono<Void> publishEmailStatusEvent(EmailStatusEvent event) {
    return Mono.fromCallable(
            () -> {
              log.info("Publishing email status event: {}", event);
              return kafkaTemplate.send(EMAIL_EVENTS_TOPIC, "email-status", event);
            })
        .then();
  }
}
