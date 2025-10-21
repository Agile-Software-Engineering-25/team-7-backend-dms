package com.ase.dms.services;

import com.ase.dms.config.MinioConfig;
import com.ase.dms.exceptions.MinIODeleteObjectDataException;
import com.ase.dms.exceptions.MinIOGetObjectDataException;

import com.ase.dms.exceptions.MinIOSetObjectDataException;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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
  public byte[] getObjectData(String fileId) {
    try (InputStream stream = minioConfig.minioClient().getObject(
        GetObjectArgs.builder()
            .bucket(minioConfig.getBucketName())
            .object(fileId)
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
      throw new MinIOGetObjectDataException(fileId);
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

      LOGGER.error("Failed to delete File with ID: ${objectName}", e);
      throw new MinIODeleteObjectDataException(objectName);
    }
  }

  @Override
  public void setObject(String objectName, byte[] data) {
    try {

      // Convert byte[] to InputStream
      InputStream inputStream = new ByteArrayInputStream(data);
      long size = data.length;
      long partSize = calculatePartSize(size);

      minioConfig.minioClient().putObject(
          PutObjectArgs.builder()
              .bucket(minioConfig.getBucketName())
              .object(objectName)
              .stream(inputStream, size, partSize)
              .contentType("application/octet-stream")
              .build());

    } catch (

    Exception e) {

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

      LOGGER.error("Failed to save Data with ID: ", e);
      throw new MinIOSetObjectDataException(objectName);
    }
  }

  private static long calculatePartSize(long totalSize) {
    final long MIN_PART_SIZE = 5L * 1024 * 1024; // 5 MB
    final long MAX_PARTS = 10_000;

    // Compute minimum required part size
    long requiredPartSize = (long) Math.ceil((double) totalSize / MAX_PARTS);

    // Ensure it's not smaller than 5 MB
    return Math.max(requiredPartSize, MIN_PART_SIZE);
  }
}
