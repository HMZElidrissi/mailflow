package com.mailflow.contactservice.controller;

import com.mailflow.contactservice.dto.ContactDTO;
import com.mailflow.contactservice.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

  private final ContactService contactService;

  @PostMapping
  public ResponseEntity<ContactDTO.Response> createContact(
      @Valid @RequestBody ContactDTO.Request request) {
    return new ResponseEntity<>(contactService.createContact(request), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ContactDTO.Response> updateContact(
      @PathVariable Long id, @Valid @RequestBody ContactDTO.Request request) {
    return ResponseEntity.ok(contactService.updateContact(id, request));
  }

  @PostMapping("/{id}/tags")
  public ResponseEntity<ContactDTO.Response> addTags(
      @PathVariable Long id, @Valid @RequestBody ContactDTO.TagOperation tagOperation) {
    return ResponseEntity.ok(contactService.addTags(id, tagOperation.getTags()));
  }

  @GetMapping("/search")
  public ResponseEntity<List<ContactDTO.Response>> searchContacts(@RequestParam String query) {
    return ResponseEntity.ok(contactService.searchContacts(query));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
    contactService.deleteContact(id);
    return ResponseEntity.noContent().build();
  }
}
