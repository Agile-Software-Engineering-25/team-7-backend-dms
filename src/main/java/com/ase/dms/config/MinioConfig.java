package com.ase.dms.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Getter
@Setter
public class MinioConfig {

  private String endpoint;
  private int port;
  private boolean tls;
  private String accessKey;
  private String accessSecret;
  private String bucketName;

  @Bean
  public MinioClient minioClient() {

    return MinioClient.builder()
        .endpoint(endpoint, port, tls)
        .credentials(accessKey, accessSecret)
        .build();
  }
}
