package com.mailflow.contactservice.service;

import com.mailflow.contactservice.domain.Contact;
import com.mailflow.contactservice.dto.contact.ContactRequest;
import com.mailflow.contactservice.dto.contact.ContactResponse;
import com.mailflow.contactservice.dto.contact.ContactTaggedEvent;
import com.mailflow.contactservice.dto.response.PageResponse;
import com.mailflow.contactservice.exception.ContactAlreadyExistsException;
import com.mailflow.contactservice.exception.ResourceNotFoundException;
import com.mailflow.contactservice.mapper.ContactMapper;
import com.mailflow.contactservice.repository.ContactRepository;
import com.mailflow.contactservice.service.impl.ContactServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.*;

import static com.mailflow.contactservice.config.KafkaConfig.CONTACT_EVENTS_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContactServiceTest {

  @Mock private ContactRepository contactRepository;

  @Mock private ContactMapper contactMapper;

  @Mock private KafkaTemplate<String, Object> kafkaTemplate;

  @InjectMocks private ContactServiceImpl contactService;

  private Contact contact;
  private ContactResponse contactResponse;
  private ContactRequest contactRequest;

  @BeforeEach
  void setUp() {
    contact =
        Contact.builder()
            .id(1L)
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli")
            .tags(new HashSet<>(Arrays.asList("newsletter", "customer")))
            .build();

    contactResponse =
        ContactResponse.builder()
            .id(1L)
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli")
            .tags(Set.of("newsletter", "customer"))
            .createdAt("2025-03-23 12:00:00")
            .updatedAt("2025-03-23 12:00:00")
            .build();

    contactRequest =
        ContactRequest.builder()
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli")
            .tags(Set.of("newsletter", "customer"))
            .build();
  }

  @Test
  @DisplayName("Should find contacts by tag")
  void shouldFindContactsByTag() {
    // Arrange
    String tag = "newsletter";

    Contact contact1 =
        Contact.builder()
            .id(1L)
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli")
            .tags(new HashSet<>(Arrays.asList("newsletter", "customer")))
            .build();

    Contact contact2 =
        Contact.builder()
            .id(2L)
            .email("latifa.chakir@gmail.com")
            .firstName("Latifa")
            .lastName("Waheli")
            .tags(new HashSet<>(Collections.singletonList("newsletter")))
            .build();

    ContactResponse response1 =
        ContactResponse.builder()
            .id(1L)
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli")
            .tags(Set.of("newsletter", "customer"))
            .createdAt("2025-03-23 12:00:00")
            .updatedAt("2025-03-23 12:00:00")
            .build();

    ContactResponse response2 =
        ContactResponse.builder()
            .id(2L)
            .email("latifa.chakir@gmail.com")
            .firstName("Latifa")
            .lastName("Waheli")
            .tags(Set.of("newsletter"))
            .createdAt("2023-01-02 12:00:00")
            .updatedAt("2023-01-02 12:00:00")
            .build();

    List<Contact> matchingContacts = List.of(contact1, contact2);

    when(contactRepository.findByTag(tag)).thenReturn(matchingContacts);
    when(contactMapper.toResponse(contact1)).thenReturn(response1);
    when(contactMapper.toResponse(contact2)).thenReturn(response2);

    // Act
    List<ContactResponse> result = contactService.findContactsByTag(tag);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result.get(0).email()).isEqualTo("khalid.waheli@gmail.com");
    assertThat(result.get(1).email()).isEqualTo("latifa.chakir@gmail.com");

    verify(contactRepository).findByTag(tag);
    verify(contactMapper).toResponse(contact1);
    verify(contactMapper).toResponse(contact2);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent contact")
  void shouldThrowExceptionWhenDeletingNonExistentContact() {
    // Arrange
    when(contactRepository.existsById(99L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> contactService.deleteContact(99L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Contact not found with ID: 99");

    verify(contactRepository).existsById(99L);
    verify(contactRepository, never()).deleteById(anyLong());
  }

  @Test
  @DisplayName("Should delete contact successfully")
  void shouldDeleteContact() {
    // Arrange
    when(contactRepository.existsById(1L)).thenReturn(true);
    doNothing().when(contactRepository).deleteById(1L);

    // Act
    contactService.deleteContact(1L);

    // Assert
    verify(contactRepository).existsById(1L);
    verify(contactRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should search contacts by query")
  void shouldSearchContacts() {
    // Arrange
    String searchQuery = "john";
    Contact matchingContact =
        Contact.builder()
            .id(1L)
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli")
            .tags(new HashSet<>(Arrays.asList("newsletter", "customer")))
            .build();

    Pageable pageable = PageRequest.of(0, 10);
    Page<Contact> contactPage = new PageImpl<>(List.of(matchingContact), pageable, 1);

    when(contactRepository.searchContacts(searchQuery, pageable)).thenReturn(contactPage);
    when(contactMapper.toResponse(matchingContact)).thenReturn(contactResponse);

    // Act
    PageResponse<ContactResponse> result = contactService.searchContacts(searchQuery, pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).firstName()).isEqualTo("Khalid");
    assertThat(result.getContent().get(0).email()).isEqualTo("khalid.waheli@gmail.com");

    verify(contactRepository).searchContacts(searchQuery, pageable);
    verify(contactMapper).toResponse(matchingContact);
  }

  @Test
  @DisplayName("Should create contact successfully")
  void shouldCreateContact() {
    // Arrange
    when(contactRepository.existsByEmail(contactRequest.email())).thenReturn(false);
    when(contactMapper.toEntity(contactRequest)).thenReturn(contact);
    when(contactRepository.save(contact)).thenReturn(contact);
    when(contactMapper.toResponse(contact)).thenReturn(contactResponse);
    when(kafkaTemplate.send(eq(CONTACT_EVENTS_TOPIC), any(ContactTaggedEvent.class)))
        .thenReturn(null);

    // Act
    ContactResponse result = contactService.createContact(contactRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.email()).isEqualTo("khalid.waheli@gmail.com");
    assertThat(result.firstName()).isEqualTo("Khalid");
    assertThat(result.lastName()).isEqualTo("Waheli");
    assertThat(result.tags()).containsExactlyInAnyOrder("newsletter", "customer");

    verify(contactRepository).existsByEmail(contactRequest.email());
    verify(contactMapper).toEntity(contactRequest);
    verify(contactRepository).save(contact);
    verify(contactMapper).toResponse(contact);

    ArgumentCaptor<ContactTaggedEvent> eventCaptor =
        ArgumentCaptor.forClass(ContactTaggedEvent.class);
    verify(kafkaTemplate, times(2)).send(eq(CONTACT_EVENTS_TOPIC), eventCaptor.capture());

    List<ContactTaggedEvent> capturedEvents = eventCaptor.getAllValues();
    assertThat(capturedEvents).hasSize(2);
    assertThat(capturedEvents.stream().map(ContactTaggedEvent::tag))
        .containsExactlyInAnyOrder("newsletter", "customer");
  }

  @Test
  @DisplayName("Should throw exception when creating contact with existing email")
  void shouldThrowExceptionWhenCreatingContactWithExistingEmail() {
    // Arrange
    when(contactRepository.existsByEmail(contactRequest.email())).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> contactService.createContact(contactRequest))
        .isInstanceOf(ContactAlreadyExistsException.class)
        .hasMessageContaining("Contact with email khalid.waheli@gmail.com already exists");

    verify(contactRepository).existsByEmail(contactRequest.email());
    verifyNoInteractions(contactMapper);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  @DisplayName("Should update contact successfully")
  void shouldUpdateContact() {
    // Arrange
    ContactRequest updateRequest =
        ContactRequest.builder()
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli Updated")
            .tags(Set.of("newsletter", "vip"))
            .build();

    Contact updatedContact =
        Contact.builder()
            .id(1L)
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli Updated")
            .tags(new HashSet<>(Arrays.asList("newsletter", "vip")))
            .build();

    ContactResponse updatedResponse =
        ContactResponse.builder()
            .id(1L)
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli Updated")
            .tags(Set.of("newsletter", "vip"))
            .createdAt("2025-03-23 12:00:00")
            .updatedAt("2025-03-23 13:00:00")
            .build();

    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
    doNothing().when(contactMapper).updateContactFromDto(updateRequest, contact);
    when(contactRepository.save(contact)).thenReturn(updatedContact);
    when(contactMapper.toResponse(updatedContact)).thenReturn(updatedResponse);

    // Act
    ContactResponse result = contactService.updateContact(1L, updateRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.lastName()).isEqualTo("Waheli Updated");
    assertThat(result.tags()).containsExactlyInAnyOrder("newsletter", "vip");

    verify(contactRepository).findById(1L);
    verify(contactMapper).updateContactFromDto(updateRequest, contact);
    verify(contactRepository).save(contact);
    verify(contactMapper).toResponse(updatedContact);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent contact")
  void shouldThrowExceptionWhenUpdatingNonExistentContact() {
    // Arrange
    when(contactRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> contactService.updateContact(99L, contactRequest))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Contact not found with ID: 99");

    verify(contactRepository).findById(99L);
    verifyNoMoreInteractions(contactRepository);
    verifyNoInteractions(contactMapper);
  }

  @Test
  @DisplayName("Should throw exception when updating contact with existing email")
  void shouldThrowExceptionWhenUpdatingContactWithExistingEmail() {
    // Arrange
    ContactRequest updateRequest =
        ContactRequest.builder()
            .email("latifa.chakir@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli")
            .tags(Set.of("newsletter", "customer"))
            .build();

    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
    when(contactRepository.existsByEmail("latifa.chakir@gmail.com")).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> contactService.updateContact(1L, updateRequest))
        .isInstanceOf(ContactAlreadyExistsException.class)
        .hasMessageContaining("Contact with email latifa.chakir@gmail.com already exists");

    verify(contactRepository).findById(1L);
    verify(contactRepository).existsByEmail("latifa.chakir@gmail.com");
  }

  @Test
  @DisplayName("Should add tags to contact successfully")
  void shouldAddTagsToContact() {
    // Arrange
    Set<String> tagsToAdd = Set.of("premium", "active");

    Contact updatedContact =
        Contact.builder()
            .id(1L)
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli")
            .tags(new HashSet<>(Arrays.asList("newsletter", "customer", "premium", "active")))
            .build();

    ContactResponse updatedResponse =
        ContactResponse.builder()
            .id(1L)
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli")
            .tags(Set.of("newsletter", "customer", "premium", "active"))
            .createdAt("2025-03-23 12:00:00")
            .updatedAt("2025-03-23 13:00:00")
            .build();

    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
    when(contactRepository.save(contact)).thenReturn(updatedContact);
    when(contactMapper.toResponse(updatedContact)).thenReturn(updatedResponse);
    when(kafkaTemplate.send(eq(CONTACT_EVENTS_TOPIC), any(ContactTaggedEvent.class)))
        .thenReturn(null);

    // Act
    ContactResponse result = contactService.addTags(1L, tagsToAdd);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.tags())
        .containsExactlyInAnyOrder("newsletter", "customer", "premium", "active");

    verify(contactRepository).findById(1L);
    verify(contactRepository).save(contact);
    verify(contactMapper).toResponse(updatedContact);

    ArgumentCaptor<ContactTaggedEvent> eventCaptor =
        ArgumentCaptor.forClass(ContactTaggedEvent.class);
    verify(kafkaTemplate, times(2)).send(eq(CONTACT_EVENTS_TOPIC), eventCaptor.capture());

    List<ContactTaggedEvent> capturedEvents = eventCaptor.getAllValues();
    assertThat(capturedEvents).hasSize(2);
    assertThat(capturedEvents.stream().map(ContactTaggedEvent::tag))
        .containsExactlyInAnyOrder("premium", "active");
  }

  @Test
  @DisplayName("Should remove tags from contact successfully")
  void shouldRemoveTagsFromContact() {
    // Arrange
    Set<String> tagsToRemove = Set.of("newsletter");

    Contact updatedContact =
        Contact.builder()
            .id(1L)
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli")
            .tags(new HashSet<>(Collections.singletonList("customer")))
            .build();

    ContactResponse updatedResponse =
        ContactResponse.builder()
            .id(1L)
            .email("khalid.waheli@gmail.com")
            .firstName("Khalid")
            .lastName("Waheli")
            .tags(Set.of("customer"))
            .createdAt("2025-03-23 12:00:00")
            .updatedAt("2025-03-23 13:00:00")
            .build();

    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
    when(contactRepository.save(contact)).thenReturn(updatedContact);
    when(contactMapper.toResponse(updatedContact)).thenReturn(updatedResponse);

    // Act
    ContactResponse result = contactService.removeTags(1L, tagsToRemove);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.tags()).containsExactly("customer");

    verify(contactRepository).findById(1L);
    verify(contactRepository).save(contact);
    verify(contactMapper).toResponse(updatedContact);
    verifyNoInteractions(kafkaTemplate); // No Kafka events should be published for tag removal
  }

  @Test
  @DisplayName("Should get contact by id")
  void shouldGetContactById() {
    // Arrange
    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
    when(contactMapper.toResponse(contact)).thenReturn(contactResponse);

    // Act
    ContactResponse result = contactService.getContact(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.email()).isEqualTo("khalid.waheli@gmail.com");
    assertThat(result.firstName()).isEqualTo("Khalid");
    assertThat(result.lastName()).isEqualTo("Waheli");
    assertThat(result.tags()).containsExactlyInAnyOrder("newsletter", "customer");

    verify(contactRepository).findById(1L);
    verify(contactMapper).toResponse(contact);
  }

  @Test
  @DisplayName("Should throw exception when getting non-existent contact")
  void shouldThrowExceptionWhenGettingNonExistentContact() {
    // Arrange
    when(contactRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> contactService.getContact(99L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Contact not found with ID: 99");

    verify(contactRepository).findById(99L);
    verifyNoInteractions(contactMapper);
  }

  @Test
  @DisplayName("Should get all contacts with pagination")
  void shouldGetAllContactsWithPagination() {
    // Arrange
    Contact contact2 =
        Contact.builder()
            .id(2L)
            .email("latifa.chakir@gmail.com")
            .firstName("Latifa")
            .lastName("Waheli")
            .tags(new HashSet<>(List.of("newsletter")))
            .build();

    ContactResponse response2 =
        ContactResponse.builder()
            .id(2L)
            .email("latifa.chakir@gmail.com")
            .firstName("Latifa")
            .lastName("Waheli")
            .tags(Set.of("newsletter"))
            .createdAt("2023-01-02 12:00:00")
            .updatedAt("2023-01-02 12:00:00")
            .build();

    Pageable pageable = PageRequest.of(0, 10);
    Page<Contact> contactPage = new PageImpl<>(List.of(contact, contact2), pageable, 2);

    when(contactRepository.findAll(pageable)).thenReturn(contactPage);
    when(contactMapper.toResponse(contact)).thenReturn(contactResponse);
    when(contactMapper.toResponse(contact2)).thenReturn(response2);

    // Act
    PageResponse<ContactResponse> result = contactService.getContacts(pageable);

    // Assert
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent().get(0).email()).isEqualTo("khalid.waheli@gmail.com");
    assertThat(result.getContent().get(1).email()).isEqualTo("latifa.chakir@gmail.com");

    verify(contactRepository).findAll(pageable);
    verify(contactMapper).toResponse(contact);
    verify(contactMapper).toResponse(contact2);
  }
}
