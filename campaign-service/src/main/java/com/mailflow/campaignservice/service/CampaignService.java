package com.mailflow.campaignservice.service;

import com.mailflow.campaignservice.dto.campaign.CampaignRequest;
import com.mailflow.campaignservice.dto.campaign.CampaignResponse;
import com.mailflow.campaignservice.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CampaignService {
  CampaignResponse createCampaign(CampaignRequest request);

  CampaignResponse activateCampaign(Long id);

  CampaignResponse deactivateCampaign(Long id);

  CampaignResponse getCampaign(Long id);

  PageResponse<CampaignResponse> getAll(Pageable pageable);
}
