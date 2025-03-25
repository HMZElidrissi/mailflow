package com.mailflow.campaignservice.mapper;

import com.mailflow.campaignservice.domain.Campaign;
import com.mailflow.campaignservice.dto.campaign.CampaignRequest;
import com.mailflow.campaignservice.dto.campaign.CampaignResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CampaignMapper {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public Campaign toEntity(CampaignRequest request) {
    return Campaign.builder()
        .name(request.name())
        .triggerTag(request.triggerTag().toLowerCase())
        .templateId(request.templateId())
        .active(false)
        .build();
  }

  public CampaignResponse toResponse(Campaign campaign) {
    return CampaignResponse.builder()
        .id(campaign.getId())
        .name(campaign.getName())
        .triggerTag(campaign.getTriggerTag())
        .templateId(campaign.getTemplateId())
        .active(campaign.isActive())
        .createdAt(formatDateTime(campaign.getCreatedAt()))
        .updatedAt(formatDateTime(campaign.getUpdatedAt()))
        .build();
  }

  public void updateCampaignFromDto(CampaignRequest request, Campaign campaign) {
    campaign.setName(request.name());
    campaign.setTriggerTag(request.triggerTag().toLowerCase());
    campaign.setTemplateId(request.templateId());
  }

  private String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(DATE_FORMATTER);
  }
}
