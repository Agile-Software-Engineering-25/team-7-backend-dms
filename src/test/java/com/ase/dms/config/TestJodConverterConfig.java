package com.ase.dms.config;

import org.jodconverter.core.DocumentConverter;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Provides a mock DocumentConverter for tests so the context can load.
 */
@TestConfiguration
@Profile("test")
public class TestJodConverterConfig {

  @Bean
  public DocumentConverter documentConverter() {
    // Mock the converter to avoid needing LibreOffice in tests
    return Mockito.mock(DocumentConverter.class);
  }
}
