package com.mailflow.emailservice.client;

import com.mailflow.emailservice.dto.template.TemplateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "template-service", path = "/api/v1/templates")
public interface TemplateServiceClient {

    @GetMapping("/{id}")
    TemplateDTO getTemplate(@PathVariable("id") Long id);

    @PostMapping("/{id}/render")
    Map<String, String> renderTemplate(@PathVariable("id") Long id, @RequestBody Map<String, String> variables);
}