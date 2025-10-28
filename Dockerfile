# Multi-stage build für kleinere Container-Größe
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Dependencies cachen
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime image - Debian-based so we can install LibreOffice reliably
FROM eclipse-temurin:21-jre
ENV DEBIAN_FRONTEND=noninteractive

# Install LibreOffice (headless) and utilities (curl for healthcheck)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      libreoffice-core \
      libreoffice-writer \
      libreoffice-calc \
      libreoffice-impress \
      libreoffice-headless \
      fonts-dejavu-core \
      curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
# Non-root User für Security (Debian useradd)
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /usr/sbin/nologin -M appuser

COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

# Set office home
ENV OFFICE_HOME=/usr/lib/libreoffice

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
# ENTRYPOINT ["/bin/sh", "-c", "java -Doffice.home=$OFFICE_HOME -jar app.jar"] # Alternative with explicit office home
