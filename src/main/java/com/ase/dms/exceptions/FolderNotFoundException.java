package com.ase.dms.exceptions;

// Custom exception for folder not found
public class FolderNotFoundException extends RuntimeException {
  public FolderNotFoundException(String message) {
    super(message);
  }
}
