package com.ase.userservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class to extract user information from JWT tokens.
 * Provides convenient static methods to access authenticated user data.
 */
public class UserInformationJWT {
  
  /**
   * Get the current JWT token from the security context
   * @return JWT token or null if not authenticated
   */
  private static Jwt getCurrentJwt() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication instanceof JwtAuthenticationToken) {
      return ((JwtAuthenticationToken) authentication).getToken();
    }
    
    return null;
  }
  
  /**
   * Get the user ID 
   * @return User ID or null if not available
   */
  public static String getUserId() {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getSubject() : null;
  }

  /**
   * Get the user's email address
   * @return Email or null if not available
   */
  public static String getEmail() {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaimAsString("email") : null;
  }
  
  /**
   * Get the username 
   * @return Username or null if not available
   */
  public static String getUsername() {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaimAsString("preferred_username") : null;
  } 
  
  /**
   * Get the user's first name 
   * @return First name or null if not available
   */
  public static String getFirstName() {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaimAsString("given_name") : null;
  }
  
  /**
   * Get the user's last name 
   * @return Last name or null if not available
   */
  public static String getLastName() {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaimAsString("family_name") : null;
  }
  
  /**
   * Get all roles/groups of the user from multiple sources.
   * @return List of all unique roles or empty list if not available
   */
  public static List<String> getRoles() {
    Jwt jwt = getCurrentJwt();
    if (jwt == null) {
      return List.of();
    }
    
    List<String> allRoles = new ArrayList<>();
    
    // combine all group fields
    // groups
    List<String> groups = jwt.getClaimAsStringList("groups");
    if (groups != null) {
      allRoles.addAll(groups);
    }
    
    // realm_access.roles
    try {
      Map<String, Object> realmAccess = jwt.getClaim("realm_access");
      if (realmAccess != null && realmAccess.get("roles") instanceof List) {
        @SuppressWarnings("unchecked")
        List<String> realmRoles = (List<String>) realmAccess.get("roles");
        if (realmRoles != null) {
          allRoles.addAll(realmRoles);
        }
      }
    } catch (Exception e) {
      // Ignore parsing errors
    }
    
    // resource_access.account.roles
    try {
      Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
      if (resourceAccess != null && resourceAccess.get("account") instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> accountAccess = (Map<String, Object>) resourceAccess.get("account");
        if (accountAccess != null && accountAccess.get("roles") instanceof List) {
          @SuppressWarnings("unchecked")
          List<String> accountRoles = (List<String>) accountAccess.get("roles");
          if (accountRoles != null) {
            allRoles.addAll(accountRoles);
          }
        }
      }
    } catch (Exception e) {
    }
    
    // remove duplicates
    return allRoles.stream().distinct().toList();
  }
  
  /**
   * Check if the user has a specific role (case-insensitive).
   * Searches in groups, realm_access.roles, and resource_access.account.roles
   * 
   * @param role The role to check
   * @return true if user has the role, false otherwise
   */
  public static boolean hasRole(String role) {
    if (role == null) {
      return false;
    }
    
    List<String> roles = getRoles();
    return roles.stream()
        .anyMatch(r -> r.equalsIgnoreCase(role));
  }

  
  /**
   * Get a custom claim from the JWT
   * @param claimName Name of the claim
   * @return Claim value or null if not available
   */
  public static Object getClaim(String claimName) {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaim(claimName) : null;
  }
  
  /**
   * Get a custom claim as String
   * @param claimName Name of the claim
   * @return Claim value as String or null if not available
   */
  public static String getClaimAsString(String claimName) {
    Jwt jwt = getCurrentJwt();
    return jwt != null ? jwt.getClaimAsString(claimName) : null;
  }
  
  /**
   * Check if a user is currently authenticated
   * @return true if authenticated, false otherwise
   */
  public static boolean isAuthenticated() {
    return getCurrentJwt() != null;
  }
}
