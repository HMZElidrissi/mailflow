package com.mailflow.campaignservice.service;

import com.mailflow.campaignservice.dto.campaign.CampaignRequest;
import com.mailflow.campaignservice.dto.campaign.CampaignResponse;
import com.mailflow.campaignservice.dto.contact.ContactResponse;
import com.mailflow.campaignservice.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CampaignService {
  CampaignResponse createCampaign(CampaignRequest request);

  CampaignResponse updateCampaign(Long id, CampaignRequest request);

  CampaignResponse deleteCampaign(Long id);

  CampaignResponse activateCampaign(Long id);

  CampaignResponse deactivateCampaign(Long id);

  CampaignResponse getCampaign(Long id);

  PageResponse<CampaignResponse> searchCampaigns(String query, Pageable pageable);

  PageResponse<CampaignResponse> getAll(Pageable pageable);

  List<ContactResponse> getMatchingContacts(Long campaignId);
}
