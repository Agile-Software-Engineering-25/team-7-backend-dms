// based on this tutorial: https://www.javacodegeeks.com/2025/07/spring-boot-keycloak-role-based-authorization.html

package com.ase.dms.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  @Profile("!local")
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(new JwtAuthConverter());

    //the role always has to be capitalized
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            // Public endpoints
            .requestMatchers("/actuator/health/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

            // Read-only access for Students (GET methods only)
            .requestMatchers(HttpMethod.GET, "/v1/documents/**")
            .hasAnyRole("STUDENT", "DOZENT", "HOCHSCHULVERWALTUNGSMITARBEITER", "HVS-ADMIN")
            .requestMatchers(HttpMethod.GET, "/v1/folders/**")
            .hasAnyRole("STUDENT", "DOZENT", "HOCHSCHULVERWALTUNGSMITARBEITER", "HVS-ADMIN")

            // Write access (POST, PATCH, DELETE) - only for non-Student roles
            .requestMatchers(HttpMethod.POST, "/v1/documents/**")
            .hasAnyRole("DOZENT", "HOCHSCHULVERWALTUNGSMITARBEITER", "HVS-ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/v1/documents/**")
            .hasAnyRole("DOZENT", "HOCHSCHULVERWALTUNGSMITARBEITER", "HVS-ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/v1/documents/**")
            .hasAnyRole("DOZENT", "HOCHSCHULVERWALTUNGSMITARBEITER", "HVS-ADMIN")

            .requestMatchers(HttpMethod.POST, "/v1/folders/**")
            .hasAnyRole("DOZENT", "HOCHSCHULVERWALTUNGSMITARBEITER", "HVS-ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/v1/folders/**")
            .hasAnyRole("DOZENT", "HOCHSCHULVERWALTUNGSMITARBEITER", "HVS-ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/v1/folders/**")
            .hasAnyRole("DOZENT", "HOCHSCHULVERWALTUNGSMITARBEITER", "HVS-ADMIN")

            // All other requests require authentication
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));
    return http.build();
  }

  // FOR LOCAL TESTING - No authentication
  @Bean
  @Profile("local")
  public SecurityFilterChain filterChainLocal(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .anyRequest().permitAll());
    return http.build();
  }
}
