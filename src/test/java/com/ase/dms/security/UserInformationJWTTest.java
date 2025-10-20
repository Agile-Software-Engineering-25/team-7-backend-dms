package com.ase.dms.security;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Tests for UserInformationJWT helper class
 */
class UserInformationJWTTest {

  private Jwt testJwt;

  @BeforeEach
  void setUp() {
    // Create a mock JWT with test data matching the real Keycloak token structure
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "0b540a6e-988d-484a-9247-9e3a2f237438");
    claims.put("preferred_username", "david");
    claims.put("email", "dave@fave.com");
    claims.put("given_name", "david");
    claims.put("family_name", "daivd");
    claims.put("name", "david daivd");

    // Groups claim
    claims.put("groups", Arrays.asList(
        "default-roles-sau",
        "manage-users",
        "offline_access",
        "lecturer",
        "uma_authorization"
    ));

    // realm_access with roles
    Map<String, Object> realmAccess = new HashMap<>();
    realmAccess.put("roles", Arrays.asList(
        "default-roles-sau",
        "manage-users",
        "offline_access",
        "lecturer",
        "uma_authorization"
    ));
    claims.put("realm_access", realmAccess);

    // resource_access with account roles
    Map<String, Object> accountAccess = new HashMap<>();
    accountAccess.put("roles", Arrays.asList(
        "manage-account",
        "manage-account-links",
        "view-profile"
    ));
    Map<String, Object> resourceAccess = new HashMap<>();
    resourceAccess.put("account", accountAccess);
    claims.put("resource_access", resourceAccess);

    Map<String, Object> headers = new HashMap<>();
    headers.put("alg", "RS256");

    final int jwtExpirySeconds = 3600;
    testJwt = new Jwt(
        "test-token-value",
        Instant.now(),
        Instant.now().plusSeconds(jwtExpirySeconds),
        headers,
        claims
    );

    // Set the JWT in the security context
    JwtAuthenticationToken auth = new JwtAuthenticationToken(testJwt);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    // Clear security context after each test
    SecurityContextHolder.clearContext();
  }

  @Test
  void testGetUserId() {
    String userId = UserInformationJWT.getUserId();
    assertEquals("0b540a6e-988d-484a-9247-9e3a2f237438", userId);
  }

  @Test
  void testGetUsername() {
    String username = UserInformationJWT.getUsername();
    assertEquals("david", username);
  }

  @Test
  void testGetEmail() {
    String email = UserInformationJWT.getEmail();
    assertEquals("dave@fave.com", email);
  }

  @Test
  void testGetFirstName() {
    String firstName = UserInformationJWT.getFirstName();
    assertEquals("david", firstName);
  }

  @Test
  void testGetLastName() {
    String lastName = UserInformationJWT.getLastName();
    assertEquals("daivd", lastName);
  }

  @Test
  void testGetRoles() {
    List<String> roles = UserInformationJWT.getRoles();

    // Should contain roles from groups
    assertTrue(roles.contains("lecturer"));
    assertTrue(roles.contains("manage-users"));

    // Should contain roles from resource_access.account.roles
    assertTrue(roles.contains("manage-account"));
    assertTrue(roles.contains("view-profile"));

    // Should have at least 8 unique roles (5 from groups + 3 from account)
    final int minimumExpectedRoles = 8;
    assertTrue(roles.size() >= minimumExpectedRoles);
  }

  @Test
  void testHasRoleFromGroups() {
    assertTrue(UserInformationJWT.hasRole("lecturer"));
    assertTrue(UserInformationJWT.hasRole("manage-users"));
  }

  @Test
  void testHasRoleFromRealmAccess() {
    assertTrue(UserInformationJWT.hasRole("default-roles-sau"));
    assertTrue(UserInformationJWT.hasRole("offline_access"));
  }

  @Test
  void testHasRoleFromResourceAccess() {
    assertTrue(UserInformationJWT.hasRole("manage-account"));
    assertTrue(UserInformationJWT.hasRole("view-profile"));
    assertTrue(UserInformationJWT.hasRole("manage-account-links"));
  }

  @Test
  void testHasRoleCaseInsensitive() {
    assertTrue(UserInformationJWT.hasRole("LECTURER"));
    assertTrue(UserInformationJWT.hasRole("Manage-Account"));
    assertTrue(UserInformationJWT.hasRole("lecturer"));
  }

  @Test
  void testHasRoleNotExists() {
    assertFalse(UserInformationJWT.hasRole("admin"));
    assertFalse(UserInformationJWT.hasRole("superuser"));
  }

  @Test
  void testHasRoleWithNull() {
    assertFalse(UserInformationJWT.hasRole(null));
  }

  @Test
  void testGetClaim() {
    Object email = UserInformationJWT.getClaim("email");
    assertEquals("dave@fave.com", email);
  }

  @Test
  void testGetClaimAsString() {
    String email = UserInformationJWT.getClaimAsString("email");
    assertEquals("dave@fave.com", email);
  }

  @Test
  void testIsAuthenticated() {
    assertTrue(UserInformationJWT.isAuthenticated());
  }

  @Test
  void testIsAuthenticatedWithoutContext() {
    SecurityContextHolder.clearContext();
    assertFalse(UserInformationJWT.isAuthenticated());
  }

  @Test
  void testGetUserIdWithoutAuthentication() {
    SecurityContextHolder.clearContext();
    assertNull(UserInformationJWT.getUserId());
  }

  @Test
  void testGetRolesWithoutAuthentication() {
    SecurityContextHolder.clearContext();
    List<String> roles = UserInformationJWT.getRoles();
    assertTrue(roles.isEmpty());
  }

  @Test
  void testGetRolesNoDuplicates() {
    List<String> roles = UserInformationJWT.getRoles();
    // Check that there are no duplicates
    long uniqueCount = roles.stream().distinct().count();
    assertEquals(roles.size(), uniqueCount, "Roles list should not contain duplicates");
  }
}
