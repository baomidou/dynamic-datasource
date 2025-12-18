# Gradle Migration Guide

This project has been migrated from Maven to Gradle build system.

## Quick Start

### Build the project
```bash
./gradlew build
```

### Clean build artifacts
```bash
./gradlew clean
```

### Build without tests
```bash
./gradlew build -x test
```

### Publish to local Maven repository
```bash
./gradlew publishToMavenLocal
```

## Project Structure

The project consists of 6 modules:

1. **dynamic-datasource-creator** - Core datasource creator functionality
2. **dynamic-datasource-spring** - Spring framework integration
3. **dynamic-datasource-spring-boot-common** - Common Spring Boot autoconfiguration
4. **dynamic-datasource-spring-boot-starter** - Spring Boot 2.x starter
5. **dynamic-datasource-spring-boot3-starter** - Spring Boot 3.x starter (Java 17+)
6. **dynamic-datasource-spring-boot4-starter** - Spring Boot 4.x starter (Java 17+)

## Key Gradle Features

### Multi-version Spring Boot Support
The project supports multiple Spring Boot versions:
- Spring Boot 2.7.18 (Java 8+)
- Spring Boot 3.2.1 (Java 17+)
- Spring Boot 4.0.0 (Java 17+)

### Publishing Configuration
Publishing is configured for Maven Central (ossrh):
```bash
./gradlew publish
```

Requirements:
- Set `ossrhUsername` and `ossrhPassword` in `~/.gradle/gradle.properties`
- Configure GPG signing for releases

### GraalVM Native Image
Spring Boot 3 and 4 starters support GraalVM native image:
```bash
./gradlew nativeCompile
```

## Common Tasks

### List all available tasks
```bash
./gradlew tasks
```

### Build specific module
```bash
./gradlew :dynamic-datasource-spring:build
```

### Check dependencies
```bash
./gradlew dependencies
```

### Generate dependency insight
```bash
./gradlew dependencyInsight --dependency spring-boot
```

## Migration Notes

### Changes from Maven

1. **Build files**: `pom.xml` → `build.gradle.kts`
2. **Wrapper**: `mvnw` → `gradlew`
3. **Local cache**: `~/.m2/repository` → `~/.gradle/caches`

### Removed Files
- All `pom.xml` files
- `.mvn/` directory
- `mvnw` and `mvnw.cmd`

### Added Files
- `build.gradle.kts` (root and all modules)
- `settings.gradle.kts`
- `gradle.properties`
- `gradlew` and `gradlew.bat`
- `gradle/wrapper/` directory

## Troubleshooting

### Clean Gradle cache
```bash
rm -rf ~/.gradle/caches
./gradlew clean build --refresh-dependencies
```

### Update Gradle wrapper
```bash
./gradlew wrapper --gradle-version 8.5
```

## IDE Support

### IntelliJ IDEA
IntelliJ IDEA has built-in Gradle support. Just open the project and it will automatically detect the Gradle build.

### Eclipse
Install the Gradle Buildship plugin:
1. Help → Eclipse Marketplace
2. Search for "Buildship"
3. Install and restart
4. File → Import → Gradle → Existing Gradle Project

### VS Code
Install the "Gradle for Java" extension from the marketplace.
