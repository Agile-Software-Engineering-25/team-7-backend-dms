package com.ase.dms.exceptions;

public class TagNotFoundException extends DmsException {
  public TagNotFoundException(String tagUuid) {
    super(ErrorCodes.TAG_NOT_FOUND, "Tag with UUID '" + tagUuid + "' was not found.");
  }
}
