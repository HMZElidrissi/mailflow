package com.mailflow.templateservice.service;

import com.mailflow.templateservice.dto.response.PageResponse;
import com.mailflow.templateservice.dto.template.EmailTemplateRequest;
import com.mailflow.templateservice.dto.template.EmailTemplateResponse;

import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface EmailTemplateService {
    EmailTemplateResponse createTemplate(EmailTemplateRequest request);

    EmailTemplateResponse updateTemplate(Long id, EmailTemplateRequest request);

    EmailTemplateResponse getTemplate(Long id);

    PageResponse<EmailTemplateResponse> getTemplates(Pageable pageable);

    PageResponse<EmailTemplateResponse> searchTemplates(String query, Pageable pageable);

    void deleteTemplate(Long id);

    String renderTemplate(Long id, Map<String, String> variables);

    String renderSubject(Long id, Map<String, String> variables);
}
