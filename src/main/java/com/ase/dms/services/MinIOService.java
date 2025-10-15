package com.ase.dms.services;

public interface MinIOService {
  byte[] getObjectData(String objectName);
  void deleteObject(String objectName);
  void setObject(String objectName, byte[] data);
}
