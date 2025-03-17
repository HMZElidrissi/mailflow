package com.mailflow.campaignservice.client;

import com.mailflow.campaignservice.dto.template.TemplateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "template-service")
public interface TemplateServiceClient {

    @GetMapping("/api/v1/templates/{id}")
    TemplateResponse getTemplate(@PathVariable("id") Long id);
}