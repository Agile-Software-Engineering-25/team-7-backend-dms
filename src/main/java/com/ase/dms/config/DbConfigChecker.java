package com.ase.dms.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DbConfigChecker {

  @Value("${spring.datasource.password}")
  private String dbPassword;

  @PostConstruct
  public void checkPassword() {
    if (dbPassword != null) {
      System.out.println("DB Password length: " + dbPassword.length());
    } else {
      System.out.println("DB Password ist null");
    }
  }
}
