package com.mailflow.contactservice.service;

import com.mailflow.contactservice.dto.contact.ContactRequest;
import com.mailflow.contactservice.dto.contact.ContactResponse;
import com.mailflow.contactservice.dto.response.PageResponse;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;

public interface ContactService {

  ContactResponse createContact(ContactRequest request);

  ContactResponse updateContact(Long id, ContactRequest request);

  ContactResponse addTags(Long id, Set<String> tags);

  ContactResponse removeTags(Long id, Set<String> tags);

  PageResponse<ContactResponse> getContacts(Pageable pageable);

  ContactResponse getContact(Long id);

  PageResponse<ContactResponse> searchContacts(String query, Pageable pageable);

  void deleteContact(Long id);

  List<ContactResponse> findContactsByTag(String tag);
}
