package com.ase.dms.services;

import com.ase.dms.config.MinioConfig;
import com.ase.dms.exceptions.MinIOGetObjectDataException;

import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MinIOServiceImpl implements MinIOService {

  @Autowired
  private final MinioConfig minioConfig;

  private static final Logger LOGGER = LoggerFactory.getLogger(MinIOServiceImpl.class);

  public MinIOServiceImpl(MinioConfig minioConfig) {
    this.minioConfig = minioConfig;
  }

  @Override
  public byte[] getObjectData(String objectName) {
    try (InputStream stream = minioConfig.minioClient().getObject(
        GetObjectArgs.builder()
            .bucket(minioConfig.getBucketName())
            .object(objectName)
            .build());

        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = stream.read(buffer)) != -1) {
        baos.write(buffer, 0, bytesRead);
      }

      return baos.toByteArray();

    } catch (Exception e) {

      /**
       * ErrorResponseException - thrown to indicate S3 service returned an error
       * response.
       * InsufficientDataException - thrown to indicate not enough data available in
       * InputStream.
       * InternalException - thrown to indicate internal library error.
       * InvalidKeyException - thrown to indicate missing of HMAC SHA-256 library.
       * InvalidResponseException - thrown to indicate S3 service returned invalid or
       * no error response.
       * IOException - thrown to indicate I/O error on S3 operation.
       * NoSuchAlgorithmException - thrown to indicate missing of MD5 or SHA-256
       * digest library.
       * XmlParserException - thrown to indicate XML parsing error.
       * ServerException
       */

      LOGGER.error("getObjectData failed", e);
      throw new MinIOGetObjectDataException(objectName);
    }
  }

  @Override
  public void deleteObject(String objectName) {

    try {
      // Remove object.
      minioConfig.minioClient().removeObject(
          RemoveObjectArgs.builder()
              .bucket(minioConfig.getBucketName())
              .object(objectName)
              .build());

    } catch (Exception e) {

      /**
       * ErrorResponseException - thrown to indicate S3 service returned an error
       * response.
       * InsufficientDataException - thrown to indicate not enough data available in
       * InputStream.
       * InternalException - thrown to indicate internal library error.
       * InvalidKeyException - thrown to indicate missing of HMAC SHA-256 library.
       * InvalidResponseException - thrown to indicate S3 service returned invalid or
       * no error response.
       * IOException - thrown to indicate I/O error on S3 operation.
       * NoSuchAlgorithmException - thrown to indicate missing of MD5 or SHA-256
       * digest library.
       * XmlParserException - thrown to indicate XML parsing error.
       * ServerException
       */

      LOGGER.error("getObjectData failed", e);
      throw new MinIOGetObjectDataException(objectName);
    }
  }

  @Override
  public void setObject(String objectName, byte[] data) {
    throw new NotImplementedException();
  }
}
