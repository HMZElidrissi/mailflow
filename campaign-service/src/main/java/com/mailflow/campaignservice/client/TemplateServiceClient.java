package com.mailflow.campaignservice.client;

import com.mailflow.campaignservice.dto.template.EmailTemplateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "template-service", path = "/api/v1/templates")
public interface TemplateServiceClient {

    @GetMapping("/{id}")
    EmailTemplateResponse getTemplate(@PathVariable("id") Long id);
}