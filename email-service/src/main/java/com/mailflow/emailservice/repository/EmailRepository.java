package com.mailflow.emailservice.repository;

import com.mailflow.emailservice.domain.Email;
import com.mailflow.emailservice.domain.EmailStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface EmailRepository extends ReactiveCrudRepository<Email, Long> {

    Flux<Email> findByCampaignId(Long campaignId);

    Flux<Email> findByContactId(Long contactId);

    Mono<Email> findByTrackingId(String trackingId);

    @Query("SELECT * FROM emails WHERE campaign_id = :campaignId AND created_at >= :startDate AND created_at <= :endDate")
    Flux<Email> findByCampaignIdAndDateRange(Long campaignId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT * FROM emails WHERE status = :status AND created_at < :timeout")
    Flux<Email> findByStatusAndTimeout(EmailStatus status, LocalDateTime timeout);

    @Query("SELECT COUNT(*) FROM emails WHERE campaign_id = :campaignId AND status = :status")
    Mono<Long> countByCampaignIdAndStatus(Long campaignId, EmailStatus status);

    @Query("SELECT * FROM emails ORDER BY created_at DESC LIMIT :limit")
    Flux<Email> findRecentEmails(int limit);
}