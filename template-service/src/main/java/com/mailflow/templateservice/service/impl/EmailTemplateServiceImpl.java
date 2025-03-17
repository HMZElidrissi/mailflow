package com.mailflow.templateservice.service.impl;

import com.mailflow.templateservice.domain.EmailTemplate;
import com.mailflow.templateservice.dto.response.PageResponse;
import com.mailflow.templateservice.dto.template.EmailTemplateRequest;
import com.mailflow.templateservice.dto.template.EmailTemplateResponse;
import com.mailflow.templateservice.exception.InvalidTemplateException;
import com.mailflow.templateservice.exception.ResourceNotFoundException;
import com.mailflow.templateservice.exception.TemplateAlreadyExistsException;
import com.mailflow.templateservice.mapper.EmailTemplateMapper;
import com.mailflow.templateservice.repository.EmailTemplateRepository;
import com.mailflow.templateservice.service.EmailTemplateService;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateRepository templateRepository;
    private final EmailTemplateMapper templateMapper;

    @Transactional
    @Override
    public EmailTemplateResponse createTemplate(EmailTemplateRequest request) {
        log.info("Creating new email template with name: {}", request.name());

        if (templateRepository.existsByName(request.name())) {
            throw new TemplateAlreadyExistsException(
                    "Email template with name " + request.name() + " already exists");
        }

        EmailTemplate template = templateMapper.toEntity(request);

        if (!template.validateVariables()) {
            throw new InvalidTemplateException(
                    "Email template content contains undefined variables");
        }

        EmailTemplate savedTemplate = templateRepository.save(template);

        log.info("Template created successfully with ID: {}", savedTemplate.getId());
        return templateMapper.toResponse(savedTemplate);
    }

    @Transactional
    @Override
    public EmailTemplateResponse updateTemplate(Long id, EmailTemplateRequest request) {
        log.info("Updating email template with ID: {}", id);

        EmailTemplate template = getTemplateById(id);

        if (!template.getName().equals(request.name())
                && templateRepository.existsByName(request.name())) {
            throw new TemplateAlreadyExistsException(
                    "Email template with name " + request.name() + " already exists");
        }

        templateMapper.updateTemplateFromDto(request, template);

        if (!template.validateVariables()) {
            throw new InvalidTemplateException(
                    "Email template content contains undefined variables");
        }

        EmailTemplate updatedTemplate = templateRepository.save(template);

        log.info("Email template updated successfully with ID: {}", id);
        return templateMapper.toResponse(updatedTemplate);
    }

    @Transactional(readOnly = true)
    @Override
    public EmailTemplateResponse getTemplate(Long id) {
        log.info("Fetching email template with ID: {}", id);
        EmailTemplate template = getTemplateById(id);
        return templateMapper.toResponse(template);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<EmailTemplateResponse> getTemplates(Pageable pageable) {
        log.info("Fetching all email templates");

        Page<EmailTemplate> templates = templateRepository.findAll(pageable);
        List<EmailTemplateResponse> content =
                templates.stream().map(templateMapper::toResponse).toList();
        return PageResponse.of(content, templates);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<EmailTemplateResponse> searchTemplates(String query, Pageable pageable) {
        log.info("Searching email templates with query: {}", query);

        Page<EmailTemplate> templates = templateRepository.searchTemplates(query, pageable);
        List<EmailTemplateResponse> content =
                templates.stream().map(templateMapper::toResponse).toList();
        return PageResponse.of(content, templates);
    }

    @Transactional
    @Override
    public void deleteTemplate(Long id) {
        log.info("Deleting email template with ID: {}", id);

        EmailTemplate template = getTemplateById(id);
        templateRepository.delete(template);

        log.info("Email template deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public String renderTemplate(Long id, Map<String, String> variables) {
        log.info("Rendering template with ID: {}", id);
        EmailTemplate template = getTemplateById(id);
        return template.renderContent(variables);
    }

  @Transactional(readOnly = true)
  @Override
  public String renderSubject(Long id, Map<String, String> variables) {
    log.info("Rendering subject for template with ID: {}", id);
    EmailTemplate template = getTemplateById(id);
    return template.renderSubject(variables);
    }

  private EmailTemplate getTemplateById(Long id) {
    return templateRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + id));
  }
}
