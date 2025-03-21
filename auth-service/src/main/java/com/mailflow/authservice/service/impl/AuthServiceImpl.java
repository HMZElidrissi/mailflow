package com.mailflow.authservice.service.impl;

import com.mailflow.authservice.dto.auth.TokenResponse;
import com.mailflow.authservice.exception.AuthenticationException;
import com.mailflow.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final RestTemplate restTemplate;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    private static final String TOKEN_ENDPOINT_TEMPLATE = "%s/realms/%s/protocol/openid-connect/token";
    private static final String INTROSPECT_ENDPOINT_TEMPLATE = "%s/realms/%s/protocol/openid-connect/token/introspect";
    private static final String LOGOUT_ENDPOINT_TEMPLATE = "%s/realms/%s/protocol/openid-connect/logout";

    @Override
    public TokenResponse login(String email, String password) {
        String tokenUrl = String.format(TOKEN_ENDPOINT_TEMPLATE, authServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", "password");
        map.add("username", email);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    tokenUrl,
                    request,
                    TokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("User '{}' successfully authenticated", email);
                return response.getBody();
            } else {
                log.error("Authentication failed for user '{}' with status code: {}",
                        email, response.getStatusCode());
                throw new AuthenticationException("Invalid credentials");
            }
        } catch (RestClientException e) {
            log.error("Authentication failed for user '{}': {}", email, e.getMessage());
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        String tokenUrl = String.format(TOKEN_ENDPOINT_TEMPLATE, authServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", "refresh_token");
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    tokenUrl,
                    request,
                    TokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Token refreshed successfully");
                return response.getBody();
            } else {
                log.error("Token refresh failed with status code: {}", response.getStatusCode());
                throw new AuthenticationException("Invalid refresh token");
            }
        } catch (RestClientException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new AuthenticationException("Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    public boolean validateToken(String token) {
        String introspectUrl = String.format(INTROSPECT_ENDPOINT_TEMPLATE, authServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("token", token);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    introspectUrl,
                    request,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                boolean isActive = (boolean) response.getBody().getOrDefault("active", false);
                log.info("Token validation result: {}", isActive);
                return isActive;
            } else {
                log.error("Token validation failed with status code: {}", response.getStatusCode());
                return false;
            }
        } catch (RestClientException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void logout(String userId, String refreshToken) {
        String logoutUrl = String.format(LOGOUT_ENDPOINT_TEMPLATE, authServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    logoutUrl,
                    request,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT ||
                    response.getStatusCode() == HttpStatus.OK) {
                log.info("User '{}' successfully logged out", userId);
            } else {
                log.error("Logout failed for user '{}' with status code: {}",
                        userId, response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Logout failed for user '{}': {}", userId, e.getMessage());
        }
    }
}