package com.ase.dms.services;

import java.util.Optional;

import com.ase.dms.dtos.UserInfoDTO;

public interface UserClient {
  Optional<UserInfoDTO> fetchCurrentUser();
}