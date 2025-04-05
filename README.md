# Kotlin-Ktor-OpenAPI Example

## Overview

This project demonstrates how to generate type-safe code from API definitions using the Ktor framework and OpenAPI Generator. The project design is inspired by Clean Architecture principles, focusing on separation of concerns and dependency management.

## Tech Stack

- Kotlin
- Kotlin Coroutines
- Ktor
- OpenAPI Generator
- Kotest
- MockK

## OpenAPI Code Generation

To generate models and Paths classes from OpenAPI definition files:

```bash
# Using Gradle task
./gradlew generateApi
```

Generated files:
- Kotlin data models (`/src/main/kotlin/com/example/apiSchema/`)
- Resource path definitions (`/src/main/kotlin/com/example/api/ApiPaths.kt`)

### Customization

OpenAPI Generator configuration is defined in `build.gradle.kts`.

## Running the Application

```bash
# Start the Ktor server
./gradlew run
```

## Running Tests

```bash
# Run unit tests
./gradlew test
```
