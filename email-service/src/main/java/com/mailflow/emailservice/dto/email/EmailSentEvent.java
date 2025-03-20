package com.mailflow.emailservice.dto.email;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EmailSentEvent(
    Long emailId,
    Long campaignId,
    Long contactId,
    String recipientEmail,
    String trackingId,
    LocalDateTime sentAt) {}
