package com.mailflow.campaignservice.kafka;

import com.mailflow.campaignservice.domain.Campaign;
import com.mailflow.campaignservice.dto.campaign.CampaignTriggeredEvent;
import com.mailflow.campaignservice.dto.contact.ContactTaggedEvent;
import com.mailflow.campaignservice.repository.CampaignRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContactEventConsumer {
    private final CampaignRepository campaignRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "contact-events", groupId = "campaign-service")
    public void consume(ContactTaggedEvent event) {
        log.info("Received contact tagged event: {} - {}", event.contactId(), event.tag());

        List<Campaign> matchingCampaigns = campaignRepository.findByTriggerTagAndActive(
                event.tag().toLowerCase(), true);

        matchingCampaigns.forEach(campaign -> {
            kafkaTemplate.send("campaign-triggered",
                    new CampaignTriggeredEvent(
                            campaign.getId(),
                            event.contactId(),
                            campaign.getTemplateId()
                    )
            );
            log.info("Triggered campaign {} for contact {}",
                    campaign.getId(), event.contactId());
        });
    }
}