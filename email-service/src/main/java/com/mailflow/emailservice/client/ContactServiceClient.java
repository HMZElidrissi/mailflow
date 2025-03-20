package com.mailflow.emailservice.client;

import com.mailflow.emailservice.dto.contact.ContactDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "contact-service", path = "/api/v1/contacts")
public interface ContactServiceClient {

    @GetMapping("/{id}")
    ContactDTO getContact(@PathVariable("id") Long id);
}