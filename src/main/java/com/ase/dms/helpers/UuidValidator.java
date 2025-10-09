package com.ase.dms.helpers;

import com.ase.dms.exceptions.ErrorCodes;
import com.ase.dms.exceptions.ValidationException;
import java.util.UUID;

public class UuidValidator {
  /**
   * Checks if the given string is a valid UUID.
   * Throws the given RuntimeException if not valid.
   * @param id the string to check
   */
  public static void validateOrThrow(String id) {
    try {
      UUID.fromString(id);
    }
    catch (Exception e) {
      throw new ValidationException(ErrorCodes.VAL_INVALID_UUID,
          "Invalid UUID format: '" + id + "'");
    }
  }
}

