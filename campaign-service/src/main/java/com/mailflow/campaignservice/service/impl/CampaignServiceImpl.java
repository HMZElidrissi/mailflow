package com.mailflow.campaignservice.service.impl;

import static com.mailflow.campaignservice.config.KafkaConfig.CAMPAIGN_EVENTS_TOPIC;

import com.mailflow.campaignservice.domain.Campaign;
import com.mailflow.campaignservice.dto.campaign.*;
import com.mailflow.campaignservice.dto.response.PageResponse;
import com.mailflow.campaignservice.exception.ResourceNotFoundException;
import com.mailflow.campaignservice.mapper.CampaignMapper;
import com.mailflow.campaignservice.repository.CampaignRepository;
import com.mailflow.campaignservice.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

  private final CampaignRepository campaignRepository;
  private final CampaignMapper campaignMapper;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  @Transactional
  public CampaignResponse createCampaign(CampaignRequest request) {
    Campaign campaign = campaignMapper.toEntity(request);
    Campaign savedCampaign = campaignRepository.save(campaign);

    // Publish campaign created event
    kafkaTemplate.send(CAMPAIGN_EVENTS_TOPIC, CampaignCreatedEvent.builder()
            .campaignId(savedCampaign.getId())
            .name(savedCampaign.getName())
            .triggerTag(savedCampaign.getTriggerTag())
            .build());

    return campaignMapper.toResponse(savedCampaign);
  }

  @Override
  @Transactional
  public CampaignResponse activateCampaign(Long id) {
    Campaign campaign = getCampaignById(id);
    campaign.activate();
    Campaign savedCampaign = campaignRepository.save(campaign);

    // Publish campaign activated event
    kafkaTemplate.send(CAMPAIGN_EVENTS_TOPIC, CampaignActivatedEvent.builder()
            .campaignId(savedCampaign.getId())
            .triggerTag(savedCampaign.getTriggerTag())
            .build());

    return campaignMapper.toResponse(savedCampaign);
  }

  @Override
  @Transactional
  public CampaignResponse deactivateCampaign(Long id) {
    Campaign campaign = getCampaignById(id);
    campaign.deactivate();
    Campaign savedCampaign = campaignRepository.save(campaign);

    return campaignMapper.toResponse(savedCampaign);
  }

  @Override
  @Transactional(readOnly = true)
  public CampaignResponse getCampaign(Long id) {
    return campaignMapper.toResponse(getCampaignById(id));
  }

  @Override
  public PageResponse<CampaignResponse> getAll(Pageable pageable) {
    Page<Campaign> page = campaignRepository.findAll(pageable);
    List<CampaignResponse> content = page.getContent().stream().map(campaignMapper::toResponse).toList();
    return PageResponse.of(content, page);
  }

  private Campaign getCampaignById(Long id) {
    return campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with ID: " + id));
  }
}