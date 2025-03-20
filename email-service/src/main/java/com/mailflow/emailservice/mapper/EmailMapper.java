package com.mailflow.emailservice.mapper;

import com.mailflow.emailservice.domain.Email;
import com.mailflow.emailservice.dto.email.EmailResponse;
import org.springframework.stereotype.Component;

@Component
public class EmailMapper {
  public EmailResponse toResponse(Email email) {
    return EmailResponse.builder()
        .id(email.getId())
        .campaignId(email.getCampaignId())
        .contactId(email.getContactId())
        .recipientEmail(email.getRecipientEmail())
        .subject(email.getSubject())
        .content(email.getContent())
        .status(email.getStatus().name())
        .errorMessage(email.getErrorMessage())
        .sentAt(email.getSentAt())
        .createdAt(email.getCreatedAt())
        .build();
  }
}
