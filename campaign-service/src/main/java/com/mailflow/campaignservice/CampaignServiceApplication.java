package com.mailflow.campaignservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CampaignServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CampaignServiceApplication.class, args);
  }
}
