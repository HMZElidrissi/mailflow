package com.mailflow.campaignservice.client;

import com.mailflow.campaignservice.dto.contact.ContactResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "contact-service", path = "/api/v1/contacts")
public interface ContactServiceClient {

    @GetMapping("/by-tag")
    List<ContactResponse> findContactsByTag(@RequestParam("tag") String tag);

    @GetMapping("/{id}")
    ContactResponse getContact(@PathVariable("id") Long id);
}