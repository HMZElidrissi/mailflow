package com.mailflow.authservice.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class ApplicationConfig {

  @Value("${keycloak.auth-server-url}")
  private String authServerUrl;

  @Value("${keycloak.admin.username:admin}")
  private String adminUsername;

  @Value("${keycloak.admin.password:admin}")
  private String adminPassword;

  @Bean
  public Keycloak keycloakAdminClient() {
    return KeycloakBuilder.builder()
            .serverUrl(authServerUrl)
            .realm("master")
            .clientId("admin-cli")
            .username(adminUsername)
            .password(adminPassword)
            .build();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplateBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .readTimeout(Duration.ofSeconds(5))
        .build();
  }
}
