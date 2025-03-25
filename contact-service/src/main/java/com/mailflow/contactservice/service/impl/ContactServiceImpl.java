package com.mailflow.contactservice.service.impl;

import com.mailflow.contactservice.domain.Contact;
import com.mailflow.contactservice.dto.contact.ContactRequest;
import com.mailflow.contactservice.dto.contact.ContactResponse;
import com.mailflow.contactservice.dto.contact.ContactTaggedEvent;
import com.mailflow.contactservice.dto.response.PageResponse;
import com.mailflow.contactservice.exception.ContactAlreadyExistsException;
import com.mailflow.contactservice.exception.ResourceNotFoundException;
import com.mailflow.contactservice.mapper.ContactMapper;
import com.mailflow.contactservice.repository.ContactRepository;
import com.mailflow.contactservice.service.ContactService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mailflow.contactservice.config.KafkaConfig.CONTACT_EVENTS_TOPIC;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactServiceImpl implements ContactService {

  private final ContactRepository contactRepository;
  private final ContactMapper contactMapper;
  private KafkaTemplate<String, Object> kafkaTemplate;

  @Transactional
  @Override
  public ContactResponse createContact(ContactRequest request) {
    log.info("Creating new contact with email: {}", request.email());

    if (contactRepository.existsByEmail(request.email())) {
      throw new ContactAlreadyExistsException(
          "Contact with email " + request.email() + " already exists");
    }

    Contact contact = contactMapper.toEntity(request);
    Contact savedContact = contactRepository.save(contact);

    Set<String> tags = request.tags();

    tags.forEach(
            tag -> {
              ContactTaggedEvent event = new ContactTaggedEvent(
                      savedContact.getId(),
                      savedContact.getEmail(),
                      tag
              );
              kafkaTemplate.send(CONTACT_EVENTS_TOPIC, event);
              log.info("Published contact tagged event: {} - {}", savedContact.getId(), tag);
            }
    );

    log.info("Contact created successfully with ID: {}", savedContact.getId());
    return contactMapper.toResponse(savedContact);
  }

  @Transactional
  @Override
  public ContactResponse updateContact(Long id, ContactRequest request) {
    log.info("Updating contact with ID: {}", id);

    Contact contact =
        contactRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + id));

    if (!contact.getEmail().equals(request.email())
        && contactRepository.existsByEmail(request.email())) {
      throw new ContactAlreadyExistsException(
          "Contact with email " + request.email() + " already exists");
    }

    contactMapper.updateContactFromDto(request, contact);
    Contact updatedContact = contactRepository.save(contact);

    log.info("Contact updated successfully with ID: {}", id);
    return contactMapper.toResponse(updatedContact);
  }

  @Transactional
  @Override
  public ContactResponse addTags(Long id, Set<String> tags) {
    log.info("Adding tags to contact with ID: {}", id);

    Contact contact =
        contactRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + id));

    tags.forEach(contact::addTag);
    Contact updatedContact = contactRepository.save(contact);

    tags.forEach(tag -> {
      ContactTaggedEvent event = new ContactTaggedEvent(
              updatedContact.getId(),
              updatedContact.getEmail(),
              tag
      );
      kafkaTemplate.send(CONTACT_EVENTS_TOPIC, event);
      log.info("Published contact tagged event: {} - {}", updatedContact.getId(), tag);
    });

    log.info("Tags added successfully to contact with ID: {}", id);
    return contactMapper.toResponse(updatedContact);
  }

  @Transactional
  @Override
  public ContactResponse removeTags(Long id, Set<String> tags) {
    log.info("Removing tags from contact with ID: {}", id);

    Contact contact =
        contactRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + id));

    contact.removeTags(tags);
    Contact updatedContact = contactRepository.save(contact);

    log.info("Tags removed successfully from contact with ID: {}", id);
    return contactMapper.toResponse(updatedContact);
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponse<ContactResponse> getContacts(Pageable pageable) {
    log.info("Fetching contacts with pagination: {}", pageable);

    Page<Contact> contacts = contactRepository.findAll(pageable);
    List<ContactResponse> content =
        contacts.stream().map(contactMapper::toResponse).toList();
    return PageResponse.of(content, contacts);
  }

  @Transactional(readOnly = true)
  @Override
  public ContactResponse getContact(Long id) {
    log.info("Fetching contact with ID: {}", id);

    Contact contact =
        contactRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + id));

    return contactMapper.toResponse(contact);
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponse<ContactResponse> searchContacts(String query, Pageable pageable) {
    log.info("Searching contacts with query: {}", query);

    Page<Contact> contacts = contactRepository.searchContacts(query, pageable);
    List<ContactResponse> content =
        contacts.stream().map(contactMapper::toResponse).toList();
    return PageResponse.of(content, contacts);
  }

  @Transactional
  @Override
  public void deleteContact(Long id) {
    log.info("Deleting contact with ID: {}", id);

    if (!contactRepository.existsById(id)) {
      throw new ResourceNotFoundException("Contact not found with ID: " + id);
    }

    contactRepository.deleteById(id);
    log.info("Contact deleted successfully with ID: {}", id);
  }

  @Override
  public List<ContactResponse> findContactsByTag(String tag) {
    log.info("Finding contacts with tag: {}", tag);

    List<Contact> contacts = contactRepository.findByTag(tag);
    return contacts.stream().map(contactMapper::toResponse).toList();
  }
}
