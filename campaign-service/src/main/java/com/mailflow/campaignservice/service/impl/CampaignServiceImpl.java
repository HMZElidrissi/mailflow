package com.mailflow.campaignservice.service.impl;

import com.mailflow.campaignservice.client.ContactServiceClient;
import com.mailflow.campaignservice.client.TemplateServiceClient;
import com.mailflow.campaignservice.domain.Campaign;
import com.mailflow.campaignservice.dto.campaign.*;
import com.mailflow.campaignservice.dto.contact.ContactResponse;
import com.mailflow.campaignservice.dto.response.PageResponse;
import com.mailflow.campaignservice.exception.ResourceNotFoundException;
import com.mailflow.campaignservice.mapper.CampaignMapper;
import com.mailflow.campaignservice.repository.CampaignRepository;
import com.mailflow.campaignservice.service.CampaignService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignServiceImpl implements CampaignService {

  private final CampaignRepository campaignRepository;
  private final CampaignMapper campaignMapper;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ContactServiceClient contactServiceClient;
  private final TemplateServiceClient templateServiceClient;

  private static final String CAMPAIGN_EVENTS_TOPIC = "campaign-events";

  @Override
  @Transactional
  public CampaignResponse createCampaign(CampaignRequest request) {
    log.info("Creating new campaign with name: {}", request.name());

    // Verify template exists
    // verifyTemplateExists(request.templateId());

    Campaign campaign = campaignMapper.toEntity(request);
    Campaign savedCampaign = campaignRepository.save(campaign);

    // Publish campaign created event
    kafkaTemplate.send(CAMPAIGN_EVENTS_TOPIC, new CampaignCreatedEvent(
            savedCampaign.getId(),
            savedCampaign.getName(),
            savedCampaign.getTriggerTag(),
            savedCampaign.getTemplateId()));

    log.info("Campaign created successfully with ID: {}", savedCampaign.getId());
    return campaignMapper.toResponse(savedCampaign);
  }

  @Override
  public CampaignResponse updateCampaign(Long id, CampaignRequest request) {
    log.info("Updating campaign with ID: {}", id);

    Campaign campaign = getCampaignById(id);

    // Verify template exists
    // verifyTemplateExists(request.templateId());

    campaignMapper.updateCampaignFromDto(request, campaign);
    Campaign updatedCampaign = campaignRepository.save(campaign);

    log.info("Campaign updated successfully with ID: {}", id);
    return campaignMapper.toResponse(updatedCampaign);
  }

  @Override
  public CampaignResponse deleteCampaign(Long id) {
    log.info("Deleting campaign with ID: {}", id);

    Campaign campaign = getCampaignById(id);
    campaignRepository.delete(campaign);

    log.info("Campaign deleted successfully with ID: {}", id);
    return campaignMapper.toResponse(campaign);
  }

  @Override
  @Transactional
  public CampaignResponse activateCampaign(Long id) {
    log.info("Activating campaign with ID: {}", id);

    Campaign campaign = getCampaignById(id);

    // Verify template exists before activating
    // verifyTemplateExists(campaign.getTemplateId());

    campaign.activate();
    Campaign savedCampaign = campaignRepository.save(campaign);

    // Publish campaign activated event
    kafkaTemplate.send(CAMPAIGN_EVENTS_TOPIC, new CampaignActivatedEvent(
            savedCampaign.getId(),
            savedCampaign.getTriggerTag()));

    // Find contacts that match this campaign's trigger tag and process them
    findAndProcessMatchingContacts(savedCampaign);

    log.info("Campaign activated successfully with ID: {}", id);
    return campaignMapper.toResponse(savedCampaign);
  }

  @Override
  @Transactional
  public CampaignResponse deactivateCampaign(Long id) {
    log.info("Deactivating campaign with ID: {}", id);

    Campaign campaign = getCampaignById(id);
    campaign.deactivate();
    Campaign savedCampaign = campaignRepository.save(campaign);

    log.info("Campaign deactivated successfully with ID: {}", id);
    return campaignMapper.toResponse(savedCampaign);
  }

  @Override
  @Transactional(readOnly = true)
  public CampaignResponse getCampaign(Long id) {
    log.info("Fetching campaign with ID: {}", id);
    return campaignMapper.toResponse(getCampaignById(id));
  }

  @Override
  public PageResponse<CampaignResponse> searchCampaigns(String query, Pageable pageable) {
    Page<Campaign> campaigns = campaignRepository.searchCampaigns(query, pageable);
    List<CampaignResponse> content = campaigns.getContent().stream()
            .map(campaignMapper::toResponse)
            .toList();
    return PageResponse.of(content, campaigns);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<CampaignResponse> getAll(Pageable pageable) {
    log.info("Fetching all campaigns with pagination");
    Page<Campaign> page = campaignRepository.findAll(pageable);
    List<CampaignResponse> content = page.getContent().stream()
            .map(campaignMapper::toResponse)
            .toList();
    return PageResponse.of(content, page);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ContactResponse> getMatchingContacts(Long campaignId) {
    log.info("Finding contacts matching campaign ID: {}", campaignId);

    Campaign campaign = getCampaignById(campaignId);

    try {
      return contactServiceClient.findContactsByTag(campaign.getTriggerTag());
    } catch (Exception e) {
      log.error("Error fetching contacts from contact service", e);
      return Collections.emptyList();
    }
  }

  private Campaign getCampaignById(Long id) {
    return campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with ID: " + id));
  }

  private void verifyTemplateExists(Long templateId) {
    try {
      templateServiceClient.getTemplate(templateId);
    } catch (FeignException e) {
      log.error("Template with ID {} not found", templateId);
      throw new ResourceNotFoundException("Template not found with ID: " + templateId);
    }
  }

  private void findAndProcessMatchingContacts(Campaign campaign) {
    if (!campaign.isActive()) {
      return;
    }

    try {
      List<ContactResponse> contacts = contactServiceClient.findContactsByTag(campaign.getTriggerTag());
      log.info("Found {} contacts matching campaign {} trigger tag '{}'",
              contacts.size(), campaign.getId(), campaign.getTriggerTag());

      // Process each matching contact
      contacts.forEach(contact ->
              kafkaTemplate.send("campaign-triggered",
                      new CampaignTriggeredEvent(
                              campaign.getId(),
                              contact.id(),
                              campaign.getTemplateId()
                      ))
      );
    } catch (Exception e) {
      log.error("Error processing matching contacts for campaign {}", campaign.getId(), e);
    }
  }
}