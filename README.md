# Streamix – Platform Libraries

![Build](https://img.shields.io/github/actions/workflow/status/mzilin/streamix-platform/build.yml?label=Build&logo=github&logoColor=white&style=flat)
![Status](https://img.shields.io/badge/status-in_progress-yellow?label=Status)


This repository contains shared Java libraries used across all **Streamix** (Video Streaming Platform) microservices. Each library is published as a standalone Maven artifact to either Maven Local (for local development) or AWS CodeArtifact (for CI/CD and shared environments).

For a complete system overview and links to all microservices, please refer to the [Microservices Hub Repository](https://github.com/mzilin/streamix-microservices-hub).


## Table of Contents

* [Introduction](#introduction)
* [Technology Stack](#technology-stack)
* [Packages](#packages)
  * [streamix-observability](#streamix-observability)
  * [streamix-web-commons](#streamix-web-commons)
* [Publishing Packages](#publishing-packages)
  * [Publish to Maven Local](#publish-to-maven-local)
  * [Publish to AWS CodeArtifact](#publish-to-aws-codeartifact)
* [Testing](#testing)
* [License](#license)
* [Contact](#contact)


## Introduction

The **Streamix Platform** repository is a multi-module Gradle project that houses reusable shared libraries for the Streamix microservices ecosystem. Rather than duplicating cross-cutting concerns in every service, these libraries are published as versioned Maven artifacts and consumed as standard dependencies.

Each library is independently usable — services only include the modules they need.


## Technology Stack

- **Java** `21`: LTS version with enhanced performance and modern language features.
- **Spring Boot** `4.1.0`: Rapid development framework for standalone, production-ready Java apps.
- **Gradle** `9.5.1`: Powerful build tool with fast incremental builds and dependency management.
- **Maven Publish Plugin**: Packages and publishes each library as a Maven artifact.
- **AWS CodeArtifact**: Hosts published artifacts for shared access across services and CI/CD pipelines.


## Packages

### streamix-observability

Shared library that propagates `correlation_id` and `user_id` through MDC (Mapped Diagnostic Context) across all transport layers — HTTP, gRPC, RabbitMQ, Kafka, and async threads.

**Artifact:**
```groovy
implementation 'com.mariuszilinskas.streamix:streamix-observability'
```

No `@EnableXxx` annotation is required. Spring Boot auto-configuration activates each integration automatically based on what is on the classpath.

#### What activates automatically

| Classpath contains       | What gets registered                                                        |
|--------------------------|-----------------------------------------------------------------------------|
| `jakarta.servlet-api`    | `StreamixLoggingFilter` — sets MDC on every inbound HTTP request            |
| `grpc-api`               | `StreamixGrpcServerInterceptor` + `StreamixGrpcClientInterceptor`           |
| `spring-rabbit`          | `StreamixRabbitConsumerInterceptor` + `StreamixRabbitProducerPostProcessor` |
| `spring-kafka`           | `StreamixKafkaConsumerInterceptor`                                          |
| `spring-core` (always)   | `StreamixMdcTaskDecorator`                                                  |

#### MDC keys set per request

| Key              | Source                                     |
|------------------|--------------------------------------------|
| `correlation_id` | `X-Correlation-Id` header / generated UUID |
| `user_id`        | `X-User-Id` header                         |
| `service`        | `spring.application.name`                  |
| `environment`    | `spring.profiles.active`                   |

Add to each service's `application.yml`:

```yaml
spring:
  application:
    name: my-service-name
  profiles:
    active: dev
```

#### Overriding a bean

All beans are guarded with `@ConditionalOnMissingBean`. Register your own bean of the same type to disable the default and provide a custom implementation.

---

### streamix-web-commons

Shared library that provides standardised HTTP error response DTOs and utilities for consistent error handling across all Streamix microservices.

**Artifact:**
```groovy
implementation 'com.mariuszilinskas.streamix:streamix-web-commons'
```

#### What it provides

| Class / Constant       | Purpose                                                                          |
|------------------------|----------------------------------------------------------------------------------|
| `ErrorResponse`        | Standard HTTP error payload: `timestamp`, `status`, `error`, `message`           |
| `FieldErrorResponse`   | Extends `ErrorResponse` with a `fieldErrors` map for per-field validation errors |
| `WebCommonsUtils`      | Holds the shared `TIMESTAMP_FORMAT` constant (`"yyyy-MM-dd hh:mm:ss"`)           |

#### Usage

Use `ErrorResponse` and `FieldErrorResponse` in global exception handlers (`@RestControllerAdvice`) to return consistent error payloads across all services:

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(
        ResourceNotFoundException ex
) {
  var body = new ErrorResponse(
          HttpStatus.NOT_FOUND.value(),
          "Not Found",
          ex.getMessage()
  );

  return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body(body);
}
```

For validation failures, use `FieldErrorResponse` to surface per-field errors:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<FieldErrorResponse> handleValidation(
        MethodArgumentNotValidException ex
) {
    Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage
            ));

    var body = new FieldErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            fieldErrors
    );
    
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(body);
}
```


## Publishing Packages

All packages share the version defined in the root `build.gradle`:

```groovy
version = '1.0.1'
```

Both libraries are published together when running from the repository root, or individually by targeting a specific subproject.

---

### Publish to Maven Local

Maven Local publishes artifacts to `~/.m2/repository/` on your machine. It is used for local development and testing before pushing to a shared registry. Re-publishing overwrites the existing artifact at the same version — no version bump is needed during development.

**Publish all packages:**
```bash
./gradlew publishToMavenLocal
```

**Publish a single package:**
```bash
./gradlew :streamix-observability:publishToMavenLocal
./gradlew :streamix-web-commons:publishToMavenLocal
```

To consume a locally published artifact in another service, add `mavenLocal()` to its `repositories` block:

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}
```

> `mavenLocal()` should appear before `mavenCentral()` so Gradle resolves the local artifact first.

---

### Publish to AWS CodeArtifact

AWS CodeArtifact is used as the shared artifact registry for CI/CD pipelines and deployed environments. Publishing requires a valid CodeArtifact token and repository URL, provided as environment variables.

**1. Authenticate with CodeArtifact and export credentials:**
```bash
export CODE_ARTIFACT_URL=https://<domain>-<account-id>.d.codeartifact.<region>.amazonaws.com/maven/<repo>/
export CODE_ARTIFACT_TOKEN=$(aws codeartifact get-authorization-token \
    --domain <domain> \
    --domain-owner <account-id> \
    --query authorizationToken \
    --output text)
```

**2. Publish all packages:**
```bash
./gradlew publish
```

**3. Publish a single package:**
```bash
./gradlew :streamix-observability:publish
./gradlew :streamix-web-commons:publish
```

> The `publish` task targets the `CodeArtifact` repository configured in `build.gradle`. It only runs if both `CODE_ARTIFACT_URL` and `CODE_ARTIFACT_TOKEN` are present in the environment.

**To consume from CodeArtifact in a microservice**, add the repository to that service's `build.gradle`:

```groovy
repositories {
    maven {
        url = uri(System.getenv("CODE_ARTIFACT_URL") ?: "")
        credentials {
            username = "aws"
            password = System.getenv("CODE_ARTIFACT_TOKEN") ?: ""
        }
    }
    mavenCentral()
}
```


## Testing

Each subproject has its own test suite. To run all tests:

```bash
./gradlew test
```

To run tests for a specific package:

```bash
./gradlew :streamix-observability:test
./gradlew :streamix-web-commons:test
```

JaCoCo coverage reports are generated automatically after each test run at:
```
<subproject>/build/reports/jacoco/test/html/index.html
```


## License

This project is private and proprietary. Unauthorised copying, modification, distribution or use of this software, via any medium, is strictly prohibited without explicit written permission from the owner.


## Contact

For any questions or clarifications about the project, please [reach out](https://www.mariuszilinskas.com/contact) to the project owner.


------
###### © 2024–present Marius Zilinskas. All rights reserved.
