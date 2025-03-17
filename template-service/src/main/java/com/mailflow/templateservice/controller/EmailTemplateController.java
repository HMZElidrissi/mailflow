package com.mailflow.templateservice.controller;

import com.mailflow.templateservice.dto.response.PageResponse;

import com.mailflow.templateservice.dto.template.EmailTemplateRequest;
import com.mailflow.templateservice.dto.template.EmailTemplateResponse;
import com.mailflow.templateservice.service.EmailTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService templateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmailTemplateResponse createTemplate(@Valid @RequestBody EmailTemplateRequest request) {
        return templateService.createTemplate(request);
    }

    @PutMapping("/{id}")
    public EmailTemplateResponse updateTemplate(
            @PathVariable Long id, @Valid @RequestBody EmailTemplateRequest request) {
        return templateService.updateTemplate(id, request);
    }

    @GetMapping("/{id}")
    public EmailTemplateResponse getTemplate(@PathVariable Long id) {
        return templateService.getTemplate(id);
    }

    @GetMapping
    public PageResponse<EmailTemplateResponse> getTemplates(Pageable pageable) {
        return templateService.getTemplates(pageable);
    }

    @GetMapping("/search")
    public PageResponse<EmailTemplateResponse> searchTemplates(
            @RequestParam String query, Pageable pageable) {
        return templateService.searchTemplates(query, pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
    }

    @PostMapping("/{id}/render")
    public Map<String, String> renderTemplate(
            @PathVariable Long id, @RequestBody Map<String, String> variables) {
        String renderedContent = templateService.renderTemplate(id, variables);
        String renderedSubject = templateService.renderSubject(id, variables);

        return Map.of(
                "subject", renderedSubject,
                "content", renderedContent
        );
    }
}