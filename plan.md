# Maven to Gradle Conversion Plan

## Overview
Convert the Spring Boot chat server application from Maven to Gradle build system while maintaining all functionality, dependencies, and configurations.

## Current State Analysis

### Maven Configuration Summary
- **Project**: `net.malevy:chatserver:0.0.1-SNAPSHOT`
- **Parent**: `spring-boot-starter-parent:3.5.3`
- **Java Version**: 21
- **Build Tool**: Maven 3.3.2 (via wrapper)
- **Special Configurations**: 
  - Lombok annotation processing
  - Mockito JDK 21+ compatibility fixes
  - Spring Boot plugin with Lombok exclusion
  - Custom argLine property for test execution

### Dependencies to Migrate
```xml
<!-- Core Spring Boot -->
spring-boot-starter-web
spring-boot-starter-websocket
spring-boot-starter-data-redis
spring-boot-starter-actuator

<!-- Development -->
spring-boot-devtools (runtime, optional)

<!-- Utilities -->
jackson-datatype-jsr310
lombok (optional)

<!-- Testing -->
spring-boot-starter-test (test scope)
```

## Conversion Steps

### Phase 1: Gradle Setup and Configuration

#### 1.1 Create Gradle Build Files
- [ ] Create `build.gradle` in `/server/` directory
- [ ] Create `settings.gradle` in `/server/` directory  
- [ ] Create `gradle.properties` for project-wide properties
- [ ] Add Gradle wrapper files (`gradlew`, `gradlew.bat`, `gradle/wrapper/`)

#### 1.2 Basic Project Configuration
```gradle
// build.gradle structure
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.3'
    id 'io.spring.dependency-management' version '1.1.6'
}

group = 'net.malevy'
version = '0.0.1-SNAPSHOT'
java.sourceCompatibility = JavaVersion.VERSION_21
```

### Phase 2: Dependencies Migration

#### 2.1 Core Dependencies
Convert Maven dependencies to Gradle format:
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'
    
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

#### 2.2 Lombok Configuration
- [ ] Configure Lombok annotation processing
- [ ] Ensure IDE integration works (annotation processing enabled)
- [ ] Verify Lombok exclusion from JAR packaging

### Phase 3: Plugin and Build Configuration Migration

#### 3.1 Spring Boot Plugin Configuration
```gradle
springBoot {
    buildInfo()
}

jar {
    enabled = false
    archiveClassifier = ''
}

bootJar {
    enabled = true
    archiveClassifier = ''
    exclude {
        group 'org.projectlombok'
        module 'lombok'
    }
}
```

#### 3.2 Test Configuration (JDK 21 + Mockito)
Critical: Migrate Maven Surefire configuration for Mockito JDK 21+ compatibility:
```gradle
test {
    useJUnitPlatform()
    
    // Mockito JDK 21+ compatibility - more robust approach
    jvmArgs += [
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED"
    ]
    
    // Ensure proper test isolation
    forkEvery = 1
}
```

#### 3.3 Java Compilation Configuration
```gradle
compileJava {
    options.annotationProcessorPath = configurations.annotationProcessor
}

compileTestJava {
    options.annotationProcessorPath = configurations.testAnnotationProcessor
}
```

### Phase 4: Gradle Wrapper and Scripts

#### 4.1 Gradle Wrapper Setup
- [ ] Initialize Gradle wrapper: `gradle wrapper --gradle-version 8.11.1`
- [ ] Verify wrapper properties in `gradle/wrapper/gradle-wrapper.properties`
- [ ] Ensure `gradle-wrapper.properties` specifies Gradle version for consistency
- [ ] Test wrapper execution: `./gradlew --version`

#### 4.2 Update CLAUDE.md
Update build commands in project documentation:
```bash
# Old Maven commands → New Gradle commands
./mvnw clean compile → ./gradlew compileJava
./mvnw spring-boot:run → ./gradlew bootRun
./mvnw test → ./gradlew test
./mvnw clean package → ./gradlew build
```

### Phase 5: Configuration File Updates

#### 5.1 Application Configuration
- [ ] Verify `application.yml` works unchanged
- [ ] Test both default and cluster profiles
- [ ] Ensure Redis configuration remains functional

#### 5.2 IDE Configuration
- [ ] Update `.gitignore` for Gradle (add `.gradle/`, `build/`)
- [ ] Remove Maven-specific IDE files if present
- [ ] Refresh IDE project after creating Gradle files
- [ ] Verify Lombok annotation processing in IDE

### Phase 6: Testing and Validation

#### 6.1 Build Verification
- [ ] Clean build: `./gradlew clean build`
- [ ] Run tests: `./gradlew test`
- [ ] Verify all transitive dependencies match Maven build
- [ ] Start application: `./gradlew bootRun`
- [ ] Verify actuator endpoints work
- [ ] Test WebSocket functionality

#### 6.2 Profile Testing
- [ ] Test default profile: `./gradlew bootRun`
- [ ] Test cluster profile: `./gradlew bootRun --args='--spring.profiles.active=cluster'`
- [ ] Verify Redis integration works in cluster mode

#### 6.3 JAR Packaging
- [ ] Build executable JAR: `./gradlew bootJar`
- [ ] Verify JAR structure (Lombok excluded, dependencies included)
- [ ] Test JAR execution: `java -jar build/libs/chatserver-0.0.1-SNAPSHOT.jar`

### Phase 7: Cleanup and Documentation

#### 7.1 Maven Cleanup
- [ ] Remove `pom.xml`
- [ ] Remove Maven wrapper files (`mvnw`, `mvnw.cmd`, `.mvn/`)
- [ ] Remove `target/` directory
- [ ] Update `.gitignore` to remove Maven patterns

#### 7.2 Documentation Updates
- [ ] Update `CLAUDE.md` with Gradle commands
- [ ] Update `README.md` if it contains build instructions
- [ ] Update any CI/CD configurations (GitHub Actions, etc.)
- [ ] Remove all Maven references from documentation
- [ ] Update any shell scripts that reference `mvnw` commands
- [ ] Check for Maven references in configuration files or comments

## Critical Success Factors

### Must Verify
1. **Lombok Integration**: Annotation processing must work in both compilation and IDE
2. **Mockito Compatibility**: JDK 21+ test execution must not show warnings
3. **Spring Profiles**: Both default and cluster profiles must function correctly
4. **DevTools**: Hot reload functionality must work in development
5. **Actuator**: Health and info endpoints must be accessible

### Common Pitfalls to Avoid
1. **Test Configuration**: Missing Mockito JDK 21 setup will cause warnings
2. **Lombok Processing**: Incorrect annotation processor configuration breaks compilation
3. **Profile Activation**: Gradle profile activation syntax differs from Maven
4. **JAR Structure**: Ensure Spring Boot plugin creates correct executable JAR

## Rollback Plan
If conversion fails:
1. Keep backup of `pom.xml` and Maven wrapper files
2. Remove all Gradle files (`build.gradle`, `settings.gradle`, `.gradle/`, `build/`)
3. Restore Maven configuration
4. Run `./mvnw clean compile` to verify restoration

## File Changes Summary

### Files to Create
- `server/build.gradle`
- `server/settings.gradle` 
- `server/gradle.properties`
- `server/gradlew` (via wrapper command)
- `server/gradlew.bat` (via wrapper command)
- `server/gradle/wrapper/gradle-wrapper.jar`
- `server/gradle/wrapper/gradle-wrapper.properties`

### Files to Modify
- `CLAUDE.md` (update build commands)
- `.gitignore` (add Gradle patterns, remove Maven patterns)

### Files to Remove (after successful conversion)
- `server/pom.xml`
- `server/mvnw`
- `server/mvnw.cmd`
- `server/.mvn/` (directory)
- `server/target/` (directory)

### Maven References to Update/Remove
- [ ] Search codebase for hardcoded Maven commands in scripts
- [ ] Update IDE run configurations that use Maven
- [ ] Remove Maven-specific entries from `.gitignore`
- [ ] Check Docker files for Maven references
- [ ] Update deployment scripts that use Maven commands

## Testing Checklist
- [ ] Clean build succeeds
- [ ] All tests pass
- [ ] Application starts correctly
- [ ] WebSocket connections work
- [ ] Actuator endpoints respond
- [ ] Hot reload works with DevTools
- [ ] Cluster profile connects to Redis
- [ ] JAR file runs independently
- [ ] No Mockito warnings in test output
- [ ] Lombok-generated code compiles correctly