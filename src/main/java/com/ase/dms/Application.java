package com.ase.dms;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(title = "DMS API", version = "1.0"),
    servers = {
        @Server(url = "${API_BASE_URL:http://localhost:8080}", description = "API Server"),
    }
)
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
