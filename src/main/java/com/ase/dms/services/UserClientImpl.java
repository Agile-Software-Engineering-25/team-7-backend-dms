package com.ase.dms.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ase.dms.dtos.UserInfoDTO;
import com.ase.dms.security.UserInformationJWT;

@Service
public class UserClientImpl implements UserClient {

  private final RestTemplate restTemplate;

  @Value("${userservice.base-url:https://sau-portal.de/team-11-api/api}")
  private String baseUrl;

  public UserClientImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public Optional<UserInfoDTO> fetchCurrentUser() {
    String userId = UserInformationJWT.getUserId();
    if (userId == null || userId.isBlank()) {
      return Optional.empty();
    }

    String url = String.format("%s/v1/users/%s", baseUrl, userId);
    try {
      ResponseEntity<UserInfoDTO> response =
          restTemplate.getForEntity(url, UserInfoDTO.class);
      return Optional.ofNullable(response.getBody());
    } catch (Exception ex) {
      return Optional.empty();
    }
  }
}

