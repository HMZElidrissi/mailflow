package com.mailflow.emailservice.service;

import com.mailflow.emailservice.dto.campaign.CampaignTriggeredEvent;
import com.mailflow.emailservice.dto.email.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface EmailService {

  Mono<EmailResponse> sendEmail(Long campaignId, Long contactId, Long templateId);

  void processTriggeredCampaign(CampaignTriggeredEvent event);

  Mono<EmailResponse> getEmail(Long id);;

  Flux<EmailResponse> getRecentEmails(int limit);

  Flux<EmailResponse> retryFailedEmails(int minutes);

  Mono<Map<String, Object>> getEmailStatsSummary();

  Mono<Map<String, Object>> getEmailAnalytics(String period);
}
