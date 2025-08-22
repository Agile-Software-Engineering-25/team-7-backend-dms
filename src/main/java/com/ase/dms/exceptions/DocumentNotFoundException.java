package com.ase.dms.exceptions;

// Custom exception for document not found
public class DocumentNotFoundException extends RuntimeException {
  public DocumentNotFoundException(String message) {
    super(message);
  }
}
