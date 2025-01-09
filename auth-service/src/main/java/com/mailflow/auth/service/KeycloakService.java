package com.mailflow.auth.service;

import com.mailflow.auth.dto.LoginRequest;
import com.mailflow.auth.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    public ResponseEntity<?> registerUser(RegisterRequest request) {
        try {
            Keycloak keycloak = getKeycloakInstance();
            UsersResource usersResource = keycloak.realm(realm).users();

            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(request.getEmail());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setRequiredActions(List.of("VERIFY_EMAIL"));

            usersResource.create(user);

            UserRepresentation createdUser = usersResource.search(request.getEmail()).get(0);
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);

            usersResource.get(createdUser.getId()).resetPassword(credential);

            return ResponseEntity.ok().body(Collections.singletonMap("message", "User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    public ResponseEntity<?> login(LoginRequest request) {
        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(authServerUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(request.getEmail())
                    .password(request.getPassword())
                    .grantType(OAuth2Constants.PASSWORD)
                    .build();

            AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid credentials"));
        }
    }

    public ResponseEntity<?> initiatePasswordReset(String email) {
        try {
            Keycloak keycloak = getKeycloakInstance();
            UsersResource usersResource = keycloak.realm(realm).users();

            UserRepresentation user = usersResource.search(email).get(0);
            usersResource.get(user.getId()).executeActionsEmail(List.of("UPDATE_PASSWORD"));

            return ResponseEntity.ok().body(Collections.singletonMap("message", "Password reset email sent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User not found"));
        }
    }

    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}