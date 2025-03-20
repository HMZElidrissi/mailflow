package com.mailflow.emailservice.dto.email;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EmailStatusEvent(
    Long emailId, String trackingId, String status, LocalDateTime timestamp, String metadata) {}
