package com.mailflow.emailservice.config;

import com.mailflow.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final EmailService emailService;

    /**
     * Retry failed emails every 15 minutes
     * Look for failed emails from the last 24 hours
     */
    @Scheduled(fixedRate = 15 * 60 * 1000) // 15 minutes
    public void retryFailedEmails() {
        log.info("Starting scheduled task: Retry failed emails");

        emailService.retryFailedEmails(24 * 60)
                .subscribe(
                        email -> log.info("Successfully retried failed email ID: {}", email.id()),
                        error -> log.error("Error during retry failed emails task: {}", error.getMessage(), error),
                        () -> log.info("Completed retry failed emails task")
                );
    }
}