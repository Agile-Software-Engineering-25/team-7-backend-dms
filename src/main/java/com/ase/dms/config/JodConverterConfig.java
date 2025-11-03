package com.ase.dms.config;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

/**
 * Provides beans for jodconverter: a started LocalOfficeManager and a DocumentConverter.
 * office.home can be provided via property `office.home` or environment variable `OFFICE_HOME`.
 */
@Configuration
@Profile("!test")
public class JodConverterConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(JodConverterConfig.class);

  @Bean(destroyMethod = "stop")
  public LocalOfficeManager officeManager(@Value("${office.home:${OFFICE_HOME:}}") String officeHome) {
    LOGGER.info("Configuring LocalOfficeManager with officeHome='{}'", officeHome);
      LocalOfficeManager.Builder builder = LocalOfficeManager.builder();
      if (officeHome != null && !officeHome.isBlank()) {
          builder.officeHome(new File(officeHome));
      }
      LocalOfficeManager manager = builder.build();
      // start manager now so that the bean is ready for conversions
      try {
          manager.start();
      }
      catch (OfficeException e) {
          throw new IllegalStateException("Could not start LocalOfficeManager (officeHome='" + officeHome + "')", e);
      }
      return manager;
  }

  @Bean
  public DocumentConverter documentConverter(LocalOfficeManager officeManager) {
    // Simple shared converter using the started office manager
    return LocalConverter.make(officeManager);
  }
}
