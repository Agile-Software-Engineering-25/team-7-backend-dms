package com.ase.dms.helpers;

import java.util.UUID;

public class UuidValidator {
  /**
   * Checks if the given string is a valid UUID.
   * Throws the given RuntimeException if not valid.
   * @param id the string to check
   * @param exception the exception to throw if invalid
   */
  public static void validateOrThrow(String id, RuntimeException exception) {
    try {
      UUID.fromString(id);
    }
    catch (Exception e) {
      throw exception;
    }
  }
}

