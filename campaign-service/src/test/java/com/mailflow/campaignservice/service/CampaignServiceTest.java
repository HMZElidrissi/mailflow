package com.mailflow.campaignservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.mailflow.campaignservice.client.ContactServiceClient;
import com.mailflow.campaignservice.client.TemplateServiceClient;
import com.mailflow.campaignservice.domain.Campaign;
import com.mailflow.campaignservice.dto.campaign.*;
import com.mailflow.campaignservice.dto.contact.ContactResponse;
import com.mailflow.campaignservice.dto.response.PageResponse;
import com.mailflow.campaignservice.dto.template.EmailTemplateResponse;
import com.mailflow.campaignservice.exception.ResourceNotFoundException;
import com.mailflow.campaignservice.mapper.CampaignMapper;
import com.mailflow.campaignservice.repository.CampaignRepository;
import com.mailflow.campaignservice.service.impl.CampaignServiceImpl;
import feign.FeignException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

@ExtendWith(MockitoExtension.class)
public class CampaignServiceTest {

  @Mock private CampaignRepository campaignRepository;

  @Mock private CampaignMapper campaignMapper;

  @Mock private KafkaTemplate<String, Object> kafkaTemplate;

  @Mock private ContactServiceClient contactServiceClient;

  @Mock private TemplateServiceClient templateServiceClient;

  @InjectMocks private CampaignServiceImpl campaignService;

  private Campaign campaign;
  private CampaignResponse campaignResponse;
  private CampaignRequest campaignRequest;

  private static final String CAMPAIGN_EVENTS_TOPIC = "campaign-events";

  @BeforeEach
  void setUp() {
    campaign =
        Campaign.builder()
            .id(1L)
            .name("Newsletter Campaign")
            .triggerTag("newsletter-subscriber")
            .templateId(10L)
            .active(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    campaignResponse =
        new CampaignResponse(
            1L,
            "Newsletter Campaign",
            "newsletter-subscriber",
            10L,
            "Welcome Email",
            false,
            campaign.getCreatedAt().toString(),
            campaign.getUpdatedAt().toString());

    campaignRequest = new CampaignRequest("Newsletter Campaign", "newsletter-subscriber", 10L);
  }

  @Test
  @DisplayName("Should create campaign successfully")
  void shouldCreateCampaign() {
    // Arrange
    when(templateServiceClient.getTemplate(campaignRequest.templateId()))
        .thenReturn(
            new EmailTemplateResponse(10L, "Welcome Email", null, null, null, null, null, null));
    when(campaignMapper.toEntity(campaignRequest)).thenReturn(campaign);
    when(campaignRepository.save(campaign)).thenReturn(campaign);
    when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponse);
    when(kafkaTemplate.send(eq(CAMPAIGN_EVENTS_TOPIC), any(CampaignCreatedEvent.class)))
        .thenReturn(null);

    // Act
    CampaignResponse result = campaignService.createCampaign(campaignRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Newsletter Campaign");
    assertThat(result.triggerTag()).isEqualTo("newsletter-subscriber");
    assertThat(result.templateId()).isEqualTo(10L);

    verify(templateServiceClient).getTemplate(campaignRequest.templateId());
    verify(campaignMapper).toEntity(campaignRequest);
    verify(campaignRepository).save(campaign);
    verify(campaignMapper).toResponse(campaign);
    verify(kafkaTemplate).send(eq(CAMPAIGN_EVENTS_TOPIC), any(CampaignCreatedEvent.class));
  }

  @Test
  @DisplayName("Should throw exception when creating campaign with non-existent template")
  void shouldThrowExceptionWhenCreatingCampaignWithNonExistentTemplate() {
    // Arrange
    when(templateServiceClient.getTemplate(anyLong())).thenThrow(FeignException.NotFound.class);

    // Act & Assert
    assertThatThrownBy(() -> campaignService.createCampaign(campaignRequest))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Template not found");

    verify(templateServiceClient).getTemplate(campaignRequest.templateId());
    verifyNoInteractions(campaignRepository);
    verifyNoInteractions(kafkaTemplate);
  }

  @Test
  @DisplayName("Should get campaign by id")
  void shouldGetCampaignById() {
    // Arrange
    when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
    when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponse);

    // Act
    CampaignResponse result = campaignService.getCampaign(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("Newsletter Campaign");
    assertThat(result.templateName()).isEqualTo("Welcome Email");
  }

  @Test
  @DisplayName("Should throw exception when getting non-existent campaign")
  void shouldThrowExceptionWhenGettingNonExistentCampaign() {
    // Arrange
    when(campaignRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> campaignService.getCampaign(99L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Campaign not found");
  }

  @Test
  @DisplayName("Should update campaign successfully")
  void shouldUpdateCampaign() {
    // Arrange
    CampaignRequest updateRequest =
        new CampaignRequest("Updated Newsletter Campaign", "newsletter-subscriber-v2", 20L);

    Campaign updatedCampaign =
        Campaign.builder()
            .id(1L)
            .name("Updated Newsletter Campaign")
            .triggerTag("newsletter-subscriber-v2")
            .templateId(20L)
            .active(false)
            .build();

    CampaignResponse updatedResponse =
        new CampaignResponse(
            1L,
            "Updated Newsletter Campaign",
            "newsletter-subscriber-v2",
            20L,
            "Welcome Email V2",
            false,
            null,
            null);

    when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
    when(templateServiceClient.getTemplate(updateRequest.templateId()))
        .thenReturn(
            new EmailTemplateResponse(20L, "Welcome Email V2", null, null, null, null, null, null));
    doNothing().when(campaignMapper).updateCampaignFromDto(updateRequest, campaign);
    when(campaignRepository.save(campaign)).thenReturn(updatedCampaign);
    when(campaignMapper.toResponse(updatedCampaign)).thenReturn(updatedResponse);

    // Act
    CampaignResponse result = campaignService.updateCampaign(1L, updateRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Updated Newsletter Campaign");
    assertThat(result.triggerTag()).isEqualTo("newsletter-subscriber-v2");
    assertThat(result.templateId()).isEqualTo(20L);

    verify(campaignRepository).findById(1L);
    verify(templateServiceClient).getTemplate(updateRequest.templateId());
    verify(campaignMapper).updateCampaignFromDto(updateRequest, campaign);
    verify(campaignRepository).save(campaign);
  }

  @Test
  @DisplayName("Should delete campaign successfully")
  void shouldDeleteCampaign() {
    // Arrange
    when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
    doNothing().when(campaignRepository).delete(campaign);
    when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponse);

    // Act
    CampaignResponse result = campaignService.deleteCampaign(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("Newsletter Campaign");

    verify(campaignRepository).findById(1L);
    verify(campaignRepository).delete(campaign);
  }

  @Test
  @DisplayName("Should activate campaign successfully")
  void shouldActivateCampaign() {
    // Arrange
    Campaign inactiveCampaign =
        Campaign.builder()
            .id(1L)
            .name("Newsletter Campaign")
            .triggerTag("newsletter-subscriber")
            .templateId(10L)
            .active(false)
            .build();

    Campaign activeCampaign =
        Campaign.builder()
            .id(1L)
            .name("Newsletter Campaign")
            .triggerTag("newsletter-subscriber")
            .templateId(10L)
            .active(true)
            .build();

    CampaignResponse activeResponse =
        new CampaignResponse(
            1L,
            "Newsletter Campaign",
            "newsletter-subscriber",
            10L,
            "Welcome Email",
            true,
            null,
            null);

    when(campaignRepository.findById(1L)).thenReturn(Optional.of(inactiveCampaign));
    when(templateServiceClient.getTemplate(10L))
        .thenReturn(
            new EmailTemplateResponse(10L, "Welcome Email", null, null, null, null, null, null));
    when(campaignRepository.save(inactiveCampaign)).thenReturn(activeCampaign);
    when(campaignMapper.toResponse(activeCampaign)).thenReturn(activeResponse);
    when(kafkaTemplate.send(eq(CAMPAIGN_EVENTS_TOPIC), any(CampaignActivatedEvent.class)))
        .thenReturn(null);
    when(contactServiceClient.findContactsByTag("newsletter-subscriber"))
        .thenReturn(Collections.emptyList());

    // Act
    CampaignResponse result = campaignService.activateCampaign(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.active()).isTrue();

    verify(campaignRepository).findById(1L);
    verify(templateServiceClient).getTemplate(10L);
    verify(campaignRepository).save(inactiveCampaign);
    verify(kafkaTemplate).send(eq(CAMPAIGN_EVENTS_TOPIC), any(CampaignActivatedEvent.class));
    verify(contactServiceClient).findContactsByTag("newsletter-subscriber");
  }

  @Test
  @DisplayName("Should deactivate campaign successfully")
  void shouldDeactivateCampaign() {
    // Arrange
    Campaign activeCampaign =
        Campaign.builder()
            .id(1L)
            .name("Newsletter Campaign")
            .triggerTag("newsletter-subscriber")
            .templateId(10L)
            .active(true)
            .build();

    Campaign inactiveCampaign =
        Campaign.builder()
            .id(1L)
            .name("Newsletter Campaign")
            .triggerTag("newsletter-subscriber")
            .templateId(10L)
            .active(false)
            .build();

    CampaignResponse inactiveResponse =
        new CampaignResponse(
            1L,
            "Newsletter Campaign",
            "newsletter-subscriber",
            10L,
            "Welcome Email",
            false,
            null,
            null);

    when(campaignRepository.findById(1L)).thenReturn(Optional.of(activeCampaign));
    when(campaignRepository.save(activeCampaign)).thenReturn(inactiveCampaign);
    when(campaignMapper.toResponse(inactiveCampaign)).thenReturn(inactiveResponse);

    // Act
    CampaignResponse result = campaignService.deactivateCampaign(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.active()).isFalse();

    verify(campaignRepository).findById(1L);
    verify(campaignRepository).save(activeCampaign);
  }

  @Test
  @DisplayName("Should get all campaigns with pagination")
  void shouldGetAllCampaignsWithPagination() {
    // Arrange
    Campaign campaign2 =
        Campaign.builder()
            .id(2L)
            .name("Promotional Campaign")
            .triggerTag("promo-subscriber")
            .templateId(20L)
            .active(true)
            .build();

    CampaignResponse response2 =
        new CampaignResponse(
            2L,
            "Promotional Campaign",
            "promo-subscriber",
            20L,
            "Promotional Email",
            true,
            null,
            null);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Campaign> campaignPage = new PageImpl<>(List.of(campaign, campaign2), pageable, 2);

    when(campaignRepository.findAll(pageable)).thenReturn(campaignPage);
    when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponse);
    when(campaignMapper.toResponse(campaign2)).thenReturn(response2);
    when(templateServiceClient.getTemplate(10L))
        .thenReturn(
            new EmailTemplateResponse(10L, "Welcome Email", null, null, null, null, null, null));
    when(templateServiceClient.getTemplate(20L))
        .thenReturn(
            new EmailTemplateResponse(
                20L, "Promotional Email", null, null, null, null, null, null));

    // Act
    PageResponse<CampaignResponse> result = campaignService.getAll(pageable);

    // Assert
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent().get(0).name()).isEqualTo("Newsletter Campaign");
    assertThat(result.getContent().get(1).name()).isEqualTo("Promotional Campaign");
  }

  @Test
  @DisplayName("Should search campaigns by query")
  void shouldSearchCampaigns() {
    // Arrange
    String searchQuery = "newsletter";
    Campaign matchingCampaign =
        Campaign.builder()
            .id(1L)
            .name("Newsletter Campaign")
            .triggerTag("newsletter-subscriber")
            .templateId(10L)
            .active(false)
            .build();

    Pageable pageable = PageRequest.of(0, 10);
    Page<Campaign> campaignPage = new PageImpl<>(List.of(matchingCampaign), pageable, 1);

    when(campaignRepository.searchCampaigns(searchQuery, pageable)).thenReturn(campaignPage);
    when(campaignMapper.toResponse(matchingCampaign)).thenReturn(campaignResponse);
    when(templateServiceClient.getTemplate(10L))
        .thenReturn(
            new EmailTemplateResponse(10L, "Welcome Email", null, null, null, null, null, null));

    // Act
    PageResponse<CampaignResponse> result = campaignService.searchCampaigns(searchQuery, pageable);

    // Assert
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("Newsletter Campaign");
    assertThat(result.getContent().get(0).triggerTag()).isEqualTo("newsletter-subscriber");

    verify(campaignRepository).searchCampaigns(searchQuery, pageable);
    verify(templateServiceClient).getTemplate(10L);
  }

  @Test
  @DisplayName("Should get matching contacts for campaign")
  void shouldGetMatchingContacts() {
    // Arrange
    ContactResponse contact1 =
        new ContactResponse(
            1L,
            "khalid@gmail.com",
            "Khalid",
            "Waheli",
            Set.of("newsletter-subscriber"),
            "2025-03-23 12:00:00",
            "2025-03-23 12:00:00");
    ContactResponse contact2 =
        new ContactResponse(
            2L,
            "latifa@gmail.com",
            "Latifa",
            "Chakir",
            Set.of("newsletter-subscriber"),
            "2025-03-23 12:00:00",
            "2025-03-23 12:00:00");

    List<ContactResponse> matchingContacts = List.of(contact1, contact2);

    when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
    when(contactServiceClient.findContactsByTag("newsletter-subscriber"))
        .thenReturn(matchingContacts);

    // Act
    List<ContactResponse> result = campaignService.getMatchingContacts(1L);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result.get(0).email()).isEqualTo("khalid@gmail.com");
    assertThat(result.get(1).email()).isEqualTo("latifa@gmail.com");

    verify(campaignRepository).findById(1L);
    verify(contactServiceClient).findContactsByTag("newsletter-subscriber");
  }

  @Test
  @DisplayName("Should handle exception when getting matching contacts fails")
  void shouldHandleExceptionWhenGettingMatchingContactsFails() {
    // Arrange
    when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
    when(contactServiceClient.findContactsByTag("newsletter-subscriber"))
        .thenThrow(new RuntimeException("Service unavailable"));

    // Act
    List<ContactResponse> result = campaignService.getMatchingContacts(1L);

    // Assert
    assertThat(result).isEmpty();

    verify(campaignRepository).findById(1L);
    verify(contactServiceClient).findContactsByTag("newsletter-subscriber");
  }

  @Test
  @DisplayName("Should process matching contacts when activating campaign")
  void shouldProcessMatchingContactsWhenActivatingCampaign() {
    // Arrange
    Campaign inactiveCampaign =
        Campaign.builder()
            .id(1L)
            .name("Newsletter Campaign")
            .triggerTag("newsletter-subscriber")
            .templateId(10L)
            .active(false)
            .build();

    Campaign activeCampaign =
        Campaign.builder()
            .id(1L)
            .name("Newsletter Campaign")
            .triggerTag("newsletter-subscriber")
            .templateId(10L)
            .active(true)
            .build();

    ContactResponse contact1 =
        new ContactResponse(
            1L,
            "khalid@gmail.com",
            "Khalid",
            "Waheli",
            Set.of("newsletter-subscriber"),
            "2025-03-23 12:00:00",
            "2025-03-23 12:00:00");
    ContactResponse contact2 =
        new ContactResponse(
            2L,
            "latifa@gmail.com",
            "Latifa",
            "Chakir",
            Set.of("newsletter-subscriber"),
            "2025-03-23 12:00:00",
            "2025-03-23 12:00:00");

    List<ContactResponse> matchingContacts = List.of(contact1, contact2);

    when(campaignRepository.findById(1L)).thenReturn(Optional.of(inactiveCampaign));
    when(templateServiceClient.getTemplate(10L))
        .thenReturn(
            new EmailTemplateResponse(10L, "Welcome Email", null, null, null, null, null, null));
    when(campaignRepository.save(inactiveCampaign)).thenReturn(activeCampaign);
    when(campaignMapper.toResponse(activeCampaign)).thenReturn(campaignResponse);
    when(contactServiceClient.findContactsByTag("newsletter-subscriber"))
        .thenReturn(matchingContacts);
    when(kafkaTemplate.send(eq(CAMPAIGN_EVENTS_TOPIC), any(CampaignActivatedEvent.class)))
        .thenReturn(null);
    when(kafkaTemplate.send(eq("campaign-triggered"), any(CampaignTriggeredEvent.class)))
        .thenReturn(null);

    // Act
    CampaignResponse result = campaignService.activateCampaign(1L);

    // Assert
    assertThat(result).isNotNull();

    verify(campaignRepository).findById(1L);
    verify(templateServiceClient).getTemplate(10L);
    verify(campaignRepository).save(inactiveCampaign);
    verify(contactServiceClient).findContactsByTag("newsletter-subscriber");
    verify(kafkaTemplate).send(eq(CAMPAIGN_EVENTS_TOPIC), any(CampaignActivatedEvent.class));

    // Verify that kafka send was called for each matching contact
    ArgumentCaptor<CampaignTriggeredEvent> eventCaptor =
        ArgumentCaptor.forClass(CampaignTriggeredEvent.class);
    verify(kafkaTemplate, times(2)).send(eq("campaign-triggered"), eventCaptor.capture());

    List<CampaignTriggeredEvent> capturedEvents = eventCaptor.getAllValues();
    assertThat(capturedEvents).hasSize(2);
    assertThat(capturedEvents.get(0).contactId()).isEqualTo(1L);
    assertThat(capturedEvents.get(1).contactId()).isEqualTo(2L);
  }
}
