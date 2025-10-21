package com.ase.dms.services;

import com.ase.dms.config.MinioConfig;
import com.ase.dms.exceptions.MinIODeleteObjectDataException;
import com.ase.dms.exceptions.MinIOGetObjectDataException;

import com.ase.dms.exceptions.MinIOSetObjectDataException;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.MinioClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinIOServiceImplTest {

  @Mock
  private MinioConfig minioConfig;

  @Mock
  private MinioClient minioClient;

  private MinIOServiceImpl minIOService;

  @BeforeEach
  void setUp() {
    when(minioConfig.minioClient()).thenReturn(minioClient);
    when(minioConfig.getBucketName()).thenReturn("test-bucket");

    minIOService = new MinIOServiceImpl(minioConfig);
  }

  // GET OBJECT

  @Test
  void getObjectData_validObject_returnsData() throws Exception {
    String objectName = "file.txt";
    byte[] expectedData = "Sample Data".getBytes();

    GetObjectResponse response = mock(GetObjectResponse.class);

    // Simulate a real stream with first read returning data, then end-of-stream
    // (-1)
    when(response.read(any(byte[].class))).thenAnswer(new Answer<Integer>() {
      private boolean dataRead = false;

      @Override
      public Integer answer(InvocationOnMock invocation) {
        byte[] buffer = invocation.getArgument(0);
        if (!dataRead) {
          byte[] data = "Sample Data".getBytes();
          System.arraycopy(data, 0, buffer, 0, data.length);
          dataRead = true;
          return data.length;
        } else {
          return -1; // end-of-stream
        }
      }
    });

    when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

    byte[] result = minIOService.getObjectData(objectName);

    assertArrayEquals(expectedData, result);
    verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
  }

  @Test
  void getObjectData_exceptionThrown_throwsCustomException() throws Exception {
    String objectName = "bad.txt";
    when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new IOException("Simulated failure"));

    assertThrows(MinIOGetObjectDataException.class, () -> minIOService.getObjectData(objectName));
  }

  // SET OBJECT

  @Test
  void setObject_validData_callsMinioPutObject() throws Exception {
    String objectName = "upload.txt";
    byte[] data = "Upload Data".getBytes();

    when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

    assertDoesNotThrow(() -> minIOService.setObject(objectName, data));
    verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
  }

  @Test
  void setObject_exceptionThrown_throwsCustomException() throws Exception {
    String objectName = "upload-fail.txt";
    byte[] data = "Upload Data".getBytes();

    doThrow(new IOException("Put failed")).when(minioClient).putObject(any(PutObjectArgs.class));

    assertThrows(MinIOSetObjectDataException.class, () -> minIOService.setObject(objectName, data));
  }

  // DELETE OBJECT

  @Test
  void deleteObject_validObject_callsMinioRemoveObject() throws Exception {
    String objectName = "delete.txt";

    doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

    assertDoesNotThrow(() -> minIOService.deleteObject(objectName));
    verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
  }

  @Test
  void deleteObject_exceptionThrown_throwsCustomException() throws Exception {
    String objectName = "delete-fail.txt";

    doThrow(new IOException("Delete failed")).when(minioClient).removeObject(any(RemoveObjectArgs.class));

    assertThrows(MinIODeleteObjectDataException.class, () -> minIOService.deleteObject(objectName));
  }
}
