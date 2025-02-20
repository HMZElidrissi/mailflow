package com.mailflow.contactservice.controller;

import com.mailflow.contactservice.dto.contact.ContactRequest;
import com.mailflow.contactservice.dto.contact.ContactResponse;
import com.mailflow.contactservice.dto.contact.TagOperation;
import com.mailflow.contactservice.dto.response.PageResponse;
import com.mailflow.contactservice.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

  private final ContactService contactService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ContactResponse createContact(@Valid @RequestBody ContactRequest request) {
    return contactService.createContact(request);
  }

  @PutMapping("/{id}")
  public ContactResponse updateContact(
      @PathVariable Long id, @Valid @RequestBody ContactRequest request) {
    return contactService.updateContact(id, request);
  }

  @PostMapping("/{id}/tags")
  public ContactResponse addTags(
      @PathVariable Long id, @Valid @RequestBody TagOperation tagOperation) {
    return contactService.addTags(id, tagOperation.tags());
  }

  @DeleteMapping("/{id}/tags")
  public ContactResponse removeTags(
      @PathVariable Long id, @Valid @RequestBody TagOperation tagOperation) {
    return contactService.removeTags(id, tagOperation.tags());
  }

  @GetMapping("/search")
  public PageResponse<ContactResponse> searchContacts(
      @RequestParam String query, Pageable pageable) {
    return contactService.searchContacts(query, pageable);
  }

  @GetMapping("/{id}")
  public ContactResponse getContact(@PathVariable Long id) {
    return contactService.getContact(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteContact(@PathVariable Long id) {
    contactService.deleteContact(id);
  }
}
