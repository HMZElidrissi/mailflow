package com.mailflow.emailservice.dto.email;

import lombok.Builder;

@Builder
public record EmailAnalyticsDTO(Integer month, Long sent, Long opened, Long clicked) {}
