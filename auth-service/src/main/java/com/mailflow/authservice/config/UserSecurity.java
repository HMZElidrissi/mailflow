package com.mailflow.authservice.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {
  public boolean isCurrentUser(String userId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
      String authenticatedUserId = jwt.getSubject();
      return userId.equals(authenticatedUserId);
    }
    return false;
  }
}
