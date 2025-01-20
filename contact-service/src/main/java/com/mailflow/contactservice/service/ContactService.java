package com.mailflow.contactservice.service;

import com.mailflow.contactservice.dto.ContactDTO;
import com.mailflow.contactservice.exception.ContactAlreadyExistsException;
import com.mailflow.contactservice.exception.ContactNotFoundException;
import com.mailflow.contactservice.mapper.ContactMapper;
import com.mailflow.contactservice.domain.Contact;
import com.mailflow.contactservice.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

  private final ContactRepository contactRepository;
  private final ContactMapper contactMapper;

  @Transactional
  public ContactDTO.Response createContact(ContactDTO.Request request) {
    log.info("Creating new contact with email: {}", request.getEmail());

    if (contactRepository.existsByEmail(request.getEmail())) {
      throw new ContactAlreadyExistsException(
          "Contact with email " + request.getEmail() + " already exists");
    }

    Contact contact = contactMapper.toEntity(request);
    Contact savedContact = contactRepository.save(contact);

    log.info("Contact created successfully with ID: {}", savedContact.getId());
    return contactMapper.toResponse(savedContact);
  }

  @Transactional
  public ContactDTO.Response updateContact(Long id, ContactDTO.Request request) {
    log.info("Updating contact with ID: {}", id);

    Contact contact =
        contactRepository
            .findById(id)
            .orElseThrow(() -> new ContactNotFoundException("Contact not found with ID: " + id));

    if (!contact.getEmail().equals(request.getEmail())
        && contactRepository.existsByEmail(request.getEmail())) {
      throw new ContactAlreadyExistsException(
          "Contact with email " + request.getEmail() + " already exists");
    }

    contactMapper.updateContactFromDto(request, contact);
    Contact updatedContact = contactRepository.save(contact);

    log.info("Contact updated successfully with ID: {}", id);
    return contactMapper.toResponse(updatedContact);
  }

  @Transactional
  public ContactDTO.Response addTags(Long id, Set<String> tags) {
    log.info("Adding tags to contact with ID: {}", id);

    Contact contact =
        contactRepository
            .findById(id)
            .orElseThrow(() -> new ContactNotFoundException("Contact not found with ID: " + id));

    tags.forEach(contact::addTag);
    Contact updatedContact = contactRepository.save(contact);

    log.info("Tags added successfully to contact with ID: {}", id);
    return contactMapper.toResponse(updatedContact);
  }

  @Transactional
  public ContactDTO.Response removeTags(Long id, Set<String> tags) {
    log.info("Removing tags from contact with ID: {}", id);

    Contact contact =
        contactRepository
            .findById(id)
            .orElseThrow(() -> new ContactNotFoundException("Contact not found with ID: " + id));

    contact.removeTags(tags);
    Contact updatedContact = contactRepository.save(contact);

    log.info("Tags removed successfully from contact with ID: {}", id);
    return contactMapper.toResponse(updatedContact);
  }

  @Transactional(readOnly = true)
  public ContactDTO.Response getContact(Long id) {
    log.info("Fetching contact with ID: {}", id);

    Contact contact =
        contactRepository
            .findById(id)
            .orElseThrow(() -> new ContactNotFoundException("Contact not found with ID: " + id));

    return contactMapper.toResponse(contact);
  }

  @Transactional(readOnly = true)
  public List<ContactDTO.Response> searchContacts(String query) {
    log.info("Searching contacts with query: {}", query);

    List<Contact> contacts = contactRepository.searchContacts(query);
    return contacts.stream().map(contactMapper::toResponse).collect(Collectors.toList());
  }

  @Transactional
  public void deleteContact(Long id) {
    log.info("Deleting contact with ID: {}", id);

    if (!contactRepository.existsById(id)) {
      throw new ContactNotFoundException("Contact not found with ID: " + id);
    }

    contactRepository.deleteById(id);
    log.info("Contact deleted successfully with ID: {}", id);
  }
}
