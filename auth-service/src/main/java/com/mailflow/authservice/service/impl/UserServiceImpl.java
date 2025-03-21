package com.mailflow.authservice.service.impl;

import com.mailflow.authservice.dto.user.UserDTO;
import com.mailflow.authservice.dto.user.UserRegistrationRequest;
import com.mailflow.authservice.dto.user.UserUpdateRequest;
import com.mailflow.authservice.exception.ResourceNotFoundException;
import com.mailflow.authservice.exception.UserAlreadyExistsException;
import com.mailflow.authservice.service.UserService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    @Override
    public UserDTO createUser(UserRegistrationRequest request) {
        UsersResource usersResource = getKeycloakUserResource();

        if (!usersResource.searchByUsername(request.email(), true).isEmpty()) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists");
        }

        UserRepresentation user = new UserRepresentation();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(request.enabled());
        user.setEmailVerified(true);
        user.setAttributes(request.attributes());

        Response response = usersResource.create(user);
        if (response.getStatus() != 201) {
            log.error("Failed to create user: {}", response.getStatusInfo().getReasonPhrase());
            throw new RuntimeException("Failed to create user");
        }

        // Get the user ID from the response
        String userId = extractCreatedId(response);

        setUserPassword(userId, request.password());

        if (request.roles() != null && !request.roles().isEmpty()) {
            addRolesToUser(userId, request.roles());
        }

        return getUserById(userId);
    }

    @Override
    public UserDTO getUserById(String userId) {
        try {
            UserRepresentation user = getKeycloakUserResource().get(userId).toRepresentation();
            return mapToUserDTO(user);
        } catch (Exception e) {
            log.error("Error getting user with ID {}: {}", userId, e.getMessage());
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }

    @Override
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String userId = jwt.getSubject();
            return getUserById(userId);
        }
        throw new ResourceNotFoundException("Current user not found");
    }

    @Override
    public UserDTO updateUser(String userId, UserUpdateRequest request) {
        try {
            UserResource userResource = getKeycloakUserResource().get(userId);
            UserRepresentation user = userResource.toRepresentation();

            if (request.email() != null) {
                user.setEmail(request.email());
            }
            if (request.firstName() != null) {
                user.setFirstName(request.firstName());
            }
            if (request.lastName() != null) {
                user.setLastName(request.lastName());
            }
            if (request.enabled() != null) {
                user.setEnabled(request.enabled());
            }
            if (request.attributes() != null) {
                user.setAttributes(request.attributes());
            }

            userResource.update(user);

            // Update roles if provided
            if (request.roles() != null) {
                // Get current roles
                List<String> currentRoles = userResource.roles().realmLevel().listAll().stream()
                        .map(RoleRepresentation::getName)
                        .toList();

                // Remove roles that are not in the new list
                List<String> rolesToRemove = currentRoles.stream()
                        .filter(role -> !request.roles().contains(role))
                        .collect(Collectors.toList());

                // Add roles that are not already assigned
                List<String> rolesToAdd = request.roles().stream()
                        .filter(role -> !currentRoles.contains(role))
                        .collect(Collectors.toList());

                if (!rolesToRemove.isEmpty()) {
                    removeRolesFromUser(userId, rolesToRemove);
                }

                if (!rolesToAdd.isEmpty()) {
                    addRolesToUser(userId, rolesToAdd);
                }
            }

            return getUserById(userId);
        } catch (Exception e) {
            log.error("Error updating user with ID {}: {}", userId, e.getMessage());
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            getKeycloakUserResource().get(userId).remove();
            log.info("User with ID {} deleted successfully", userId);
        } catch (Exception e) {
            log.error("Error deleting user with ID {}: {}", userId, e.getMessage());
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }

    @Override
    public List<UserDTO> getAllUsers(int first, int max) {
        try {
            List<UserRepresentation> users = getKeycloakUserResource().list(first, max);
            return users.stream()
                    .map(this::mapToUserDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage());
            throw new RuntimeException("Failed to get users");
        }
    }

    @Override
    public void resetPassword(String userId, String newPassword) {
        try {
            setUserPassword(userId, newPassword);
            log.info("Password reset successful for user with ID {}", userId);
        } catch (Exception e) {
            log.error("Error resetting password for user with ID {}: {}", userId, e.getMessage());
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }

    @Override
    public void addRole(String userId, String role) {
        addRolesToUser(userId, Collections.singletonList(role));
    }

    @Override
    public void removeRole(String userId, String role) {
        removeRolesFromUser(userId, Collections.singletonList(role));
    }

    private UsersResource getKeycloakUserResource() {
        return keycloakAdmin.realm(realm).users();
    }

    private RealmResource getRealmResource() {
        return keycloakAdmin.realm(realm);
    }

    private String extractCreatedId(Response response) {
        String locationHeader = response.getHeaderString("Location");
        String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
        response.close();
        return userId;
    }

    private void setUserPassword(String userId, String password) {
        UserResource userResource = getKeycloakUserResource().get(userId);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        userResource.resetPassword(credential);
    }

    private void addRolesToUser(String userId, List<String> roleNames) {
        UserResource userResource = getKeycloakUserResource().get(userId);
        List<RoleRepresentation> realmRoles = roleNames.stream()
                .map(roleName -> getRealmResource().roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());

        userResource.roles().realmLevel().add(realmRoles);
    }

    private void removeRolesFromUser(String userId, List<String> roleNames) {
        UserResource userResource = getKeycloakUserResource().get(userId);
        List<RoleRepresentation> realmRoles = roleNames.stream()
                .map(roleName -> getRealmResource().roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());

        userResource.roles().realmLevel().remove(realmRoles);
    }

    private UserDTO mapToUserDTO(UserRepresentation user) {
        // Get user roles
        List<String> roles = Collections.emptyList();
        try {
            roles = getKeycloakUserResource().get(user.getId()).roles().realmLevel().listAll().stream()
                    .map(RoleRepresentation::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not fetch roles for user {}: {}", user.getId(), e.getMessage());
        }

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .roles(roles)
                .attributes(user.getAttributes())
                .createdTimestamp(user.getCreatedTimestamp())
                .build();
    }
}