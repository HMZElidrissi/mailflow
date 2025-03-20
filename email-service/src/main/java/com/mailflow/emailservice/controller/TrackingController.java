package com.mailflow.emailservice.controller;

import com.mailflow.emailservice.domain.EmailStatus;
import com.mailflow.emailservice.dto.email.EmailStatusEvent;
import com.mailflow.emailservice.kafka.KafkaEventPublisher;
import com.mailflow.emailservice.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/t")
@RequiredArgsConstructor
@Slf4j
public class TrackingController {

    private final EmailRepository emailRepository;
    private final KafkaEventPublisher eventPublisher;

    // 1x1 transparent GIF for tracking pixel
    private static final byte[] TRACKING_PIXEL = {
            0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00, 0x01, 0x00, (byte) 0x80, 0x00,
            0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00, 0x21, (byte) 0xF9,
            0x04, 0x01, 0x0A, 0x00, 0x01, 0x00, 0x2C, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
            0x01, 0x00, 0x00, 0x02, 0x02, 0x4C, 0x01, 0x00, 0x3B
    };

    @GetMapping(value = "/{trackingId}", produces = MediaType.IMAGE_GIF_VALUE)
    public Mono<ResponseEntity<byte[]>> trackOpen(@PathVariable String trackingId) {
        log.info("Tracking email open for ID: {}", trackingId);

        return emailRepository.findByTrackingId(trackingId)
                .flatMap(email -> {
                    // Only update if not already opened or clicked
                    if (email.getStatus() != EmailStatus.OPENED &&
                            email.getStatus() != EmailStatus.CLICKED) {

                        email.setStatus(EmailStatus.OPENED);
                        email.setOpenedAt(LocalDateTime.now());

                        return emailRepository.save(email)
                                .doOnNext(updatedEmail -> {
                                    // Publish email opened event
                                    EmailStatusEvent event = EmailStatusEvent.builder()
                                            .emailId(updatedEmail.getId())
                                            .trackingId(trackingId)
                                            .status(EmailStatus.OPENED.name())
                                            .timestamp(LocalDateTime.now())
                                            .build();

                                    eventPublisher.publishEmailStatusEvent(event)
                                            .subscribe(null,
                                                    error -> log.error("Failed to publish email open event: {}",
                                                            error.getMessage(), error));
                                });
                    }
                    return Mono.just(email);
                })
                .map(email -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_GIF)
                        .body(TRACKING_PIXEL))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.IMAGE_GIF)
                        .body(TRACKING_PIXEL));
    }

    @GetMapping("/click/{trackingId}")
    public Mono<ResponseEntity<Object>> trackClick(
            @PathVariable String trackingId,
            @RequestParam String url) {

        log.info("Tracking email click for ID: {}, URL: {}", trackingId, url);

        return emailRepository.findByTrackingId(trackingId)
                .flatMap(email -> {
                    email.setStatus(EmailStatus.CLICKED);
                    email.setClickedAt(LocalDateTime.now());

                    return emailRepository.save(email)
                            .doOnNext(updatedEmail -> {
                                // Publish email clicked event
                                EmailStatusEvent event = EmailStatusEvent.builder()
                                        .emailId(updatedEmail.getId())
                                        .trackingId(trackingId)
                                        .status(EmailStatus.CLICKED.name())
                                        .timestamp(LocalDateTime.now())
                                        .metadata(url)
                                        .build();

                                eventPublisher.publishEmailStatusEvent(event)
                                        .subscribe(null,
                                                error -> log.error("Failed to publish email click event: {}",
                                                        error.getMessage(), error));
                            });
                })
                .map(email -> ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", url)
                        .build())
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", url)
                        .build());
    }
}