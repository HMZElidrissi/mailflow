package com.mailflow.campaignservice.repository;

import com.mailflow.campaignservice.domain.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {}
