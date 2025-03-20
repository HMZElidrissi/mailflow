package com.mailflow.emailservice.dto.email;

import lombok.Builder;

@Builder
public record EmailStats(
    Long campaignId,
    Long sent,
    Long delivered,
    Long opened,
    Long clicked,
    Long failed,
    Double openRate,
    Double clickRate) {}
