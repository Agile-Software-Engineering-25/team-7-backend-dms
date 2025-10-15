package com.ase.dms.services;

import com.ase.dms.config.MinioConfig;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MinIOServiceImpl implements MinIOService {

  @Autowired
  private final MinioConfig minioConfig;

  public MinIOServiceImpl (MinioConfig minioConfig) {
    this.minioConfig = minioConfig;
  }

  @Override
  public byte[] getObjectData(String objectName) {
    throw new NotImplementedException();
  }

  @Override
  public void deleteObject(String objectName) {
    throw new NotImplementedException();
  }

  @Override
  public void setObject(String objectName, byte[] data) {
    throw new NotImplementedException();
  }
}
