package com.mailflow.templateservice.mapper;

import com.mailflow.templateservice.domain.EmailTemplate;
import com.mailflow.templateservice.dto.template.EmailTemplateRequest;
import com.mailflow.templateservice.dto.template.EmailTemplateResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EmailTemplateMapper {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public EmailTemplate toEntity(EmailTemplateRequest request) {
    return EmailTemplate.builder()
        .name(request.name())
        .subject(request.subject())
        .content(request.content())
        .variables(request.variables())
        .description(request.description())
        .build();
  }

  public EmailTemplateResponse toResponse(EmailTemplate template) {
    return EmailTemplateResponse.builder()
        .id(template.getId())
        .name(template.getName())
        .subject(template.getSubject())
        .content(template.getContent())
        .variables(template.getVariables())
        .description(template.getDescription())
        .createdAt(formatDateTime(template.getCreatedAt()))
        .updatedAt(formatDateTime(template.getUpdatedAt()))
        .build();
  }

  public void updateTemplateFromDto(EmailTemplateRequest request, EmailTemplate template) {
    template.setName(request.name());
    template.setSubject(request.subject());
    template.setContent(request.content());

    if (request.variables() != null) {
      template.setVariables(request.variables());
    }

    template.setDescription(request.description());
  }

  private String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(DATE_FORMATTER);
  }
}
