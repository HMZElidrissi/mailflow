package com.mailflow.campaignservice.controller;

import com.mailflow.campaignservice.dto.campaign.CampaignRequest;
import com.mailflow.campaignservice.dto.campaign.CampaignResponse;
import com.mailflow.campaignservice.dto.contact.ContactResponse;
import com.mailflow.campaignservice.dto.response.PageResponse;
import com.mailflow.campaignservice.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
public class CampaignController {

  private final CampaignService campaignService;

  @GetMapping
  public PageResponse<CampaignResponse> getAll(Pageable pageable) {
    return campaignService.getAll(pageable);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CampaignResponse createCampaign(@Valid @RequestBody CampaignRequest request) {
    return campaignService.createCampaign(request);
  }

  @PutMapping("/{id}")
  public CampaignResponse updateCampaign(
      @PathVariable Long id, @Valid @RequestBody CampaignRequest request) {
    return campaignService.updateCampaign(id, request);
  }

  @DeleteMapping("/{id}")
  public CampaignResponse deleteCampaign(@PathVariable Long id) {
    return campaignService.deleteCampaign(id);
  }

  @GetMapping("/{id}")
  public CampaignResponse getCampaign(@PathVariable Long id) {
    return campaignService.getCampaign(id);
  }

  @GetMapping("/search")
  public PageResponse<CampaignResponse> searchContacts(
      @RequestParam String query, Pageable pageable) {
    return campaignService.searchCampaigns(query, pageable);
  }

  @PostMapping("/{id}/activate")
  public CampaignResponse activateCampaign(@PathVariable Long id) {
    return campaignService.activateCampaign(id);
  }

  @PostMapping("/{id}/deactivate")
  public CampaignResponse deactivateCampaign(@PathVariable Long id) {
    return campaignService.deactivateCampaign(id);
  }

  @GetMapping("/{id}/matching-contacts")
  public List<ContactResponse> getMatchingContacts(@PathVariable Long id) {
    return campaignService.getMatchingContacts(id);
  }
}
