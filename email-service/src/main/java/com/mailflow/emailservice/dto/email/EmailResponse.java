package com.mailflow.emailservice.dto.email;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EmailResponse(
    Long id,
    Long campaignId,
    Long contactId,
    String recipientEmail,
    String subject,
    String content,
    String status,
    String errorMessage,
    LocalDateTime sentAt,
    LocalDateTime createdAt) {}
