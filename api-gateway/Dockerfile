# ============================================================
# Dockerfile - api-gateway
# ============================================================
# Estrategia multi-stage build (mismo patron de CompletoDocker):
#   Stage 1 (build): compila el proyecto con Gradle Wrapper + JDK 21
#   Stage 2 (runtime): ejecuta solo el .jar con JRE 21 Alpine
# ============================================================

# -------------------------------------------------------
# STAGE 1: BUILD
# -------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copiar Gradle Wrapper y archivos de configuracion para cachear dependencias
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copiar codigo fuente y empaquetar solo el bootJar ejecutable
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# -------------------------------------------------------
# STAGE 2: RUNTIME
# -------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

# Puerto del microservicio de apigateway
EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]