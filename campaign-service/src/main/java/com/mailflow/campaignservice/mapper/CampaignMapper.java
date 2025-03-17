package com.mailflow.campaignservice.mapper;

import com.mailflow.campaignservice.domain.Campaign;
import com.mailflow.campaignservice.dto.campaign.CampaignRequest;
import com.mailflow.campaignservice.dto.campaign.CampaignResponse;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CampaignMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "active", constant = "false")
  @Mapping(target = "triggerTag", expression = "java(request.triggerTag().toLowerCase())")
  Campaign toEntity(CampaignRequest request);

  @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "formatDateTime")
  @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "formatDateTime")
  CampaignResponse toResponse(Campaign campaign);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "triggerTag", expression = "java(request.triggerTag().toLowerCase())")
  void updateCampaignFromDto(CampaignRequest request, @MappingTarget Campaign campaign);

  @Named("formatDateTime")
  default String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }
}
