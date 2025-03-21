package com.mailflow.authservice.service;

import com.mailflow.authservice.dto.user.UserDTO;
import com.mailflow.authservice.dto.user.UserRegistrationRequest;
import com.mailflow.authservice.dto.user.UserUpdateRequest;

import java.util.List;

public interface UserService {

    UserDTO createUser(UserRegistrationRequest request);

    UserDTO getUserById(String userId);

    UserDTO getCurrentUser();

    UserDTO updateUser(String userId, UserUpdateRequest request);

    void deleteUser(String userId);

    List<UserDTO> getAllUsers(int first, int max);

    void resetPassword(String userId, String newPassword);

    void addRole(String userId, String role);

    void removeRole(String userId, String role);
}