package com.mailflow.campaignservice.repository;

import com.mailflow.campaignservice.domain.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
  @Query(
      "SELECT DISTINCT c FROM Campaign c WHERE "
          + "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
          + "LOWER(c.triggerTag) LIKE LOWER(CONCAT('%', :query, '%'))")
  Page<Campaign> searchCampaigns(String query, Pageable pageable);
}
