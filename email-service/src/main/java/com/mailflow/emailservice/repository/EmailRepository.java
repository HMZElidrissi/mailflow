package com.mailflow.emailservice.repository;

import com.mailflow.emailservice.domain.Email;
import com.mailflow.emailservice.domain.EmailStatus;
import com.mailflow.emailservice.dto.email.EmailAnalyticsDTO;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface EmailRepository extends ReactiveCrudRepository<Email, Long> {
    Mono<Email> findByTrackingId(String trackingId);

    @Query("SELECT * FROM emails WHERE status = :status AND created_at < :timeout")
    Flux<Email> findByStatusAndTimeout(EmailStatus status, LocalDateTime timeout);

    @Query("SELECT * FROM emails ORDER BY created_at DESC LIMIT :limit")
    Flux<Email> findRecentEmails(int limit);

    Mono<Long> countByStatusAndCreatedAtAfter(EmailStatus status, LocalDateTime date);

    Mono<Long> countByStatusAndCreatedAtBetween(EmailStatus status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT EXTRACT(MONTH FROM created_at)::integer as month, " +
            "COUNT(CASE WHEN status = 'SENT' THEN 1 END) as sent, " +
            "COUNT(CASE WHEN status = 'OPENED' THEN 1 END) as opened, " +
            "COUNT(CASE WHEN status = 'CLICKED' THEN 1 END) as clicked " +
            "FROM emails " +
            "WHERE created_at >= :startDate " +
            "GROUP BY EXTRACT(MONTH FROM created_at) " +
            "ORDER BY month")
    Flux<EmailAnalyticsDTO> getEmailAnalytics(LocalDateTime startDate);
}