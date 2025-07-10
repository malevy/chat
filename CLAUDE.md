# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is a Spring Boot chat server application with the following structure:
- `server/` - Spring Boot application root
- `server/src/main/java/net/malevy/chatserver/` - Main application code
- `server/src/test/java/net/malevy/chatserver/` - Test code
- `server/src/main/resources/` - Configuration and static resources

## Common Commands

All commands should be run from the `server/` directory:

### Build and Run
```bash
cd server
./mvnw clean compile
./mvnw spring-boot:run
```

### Testing
```bash
cd server
./mvnw test
```

### Package
```bash
cd server
./mvnw clean package
```

## Architecture

- **Framework**: Spring Boot 3.5.3 with Java 21
- **Dependencies**: 
  - Spring Web (REST endpoints)
  - Spring WebSocket (real-time communication)
  - Spring Boot DevTools (development hot reload)
  - Lombok (boilerplate code reduction)
- **Build Tool**: Maven with wrapper scripts
- **Main Class**: `ServerApplication.java` - standard Spring Boot entry point
- **Package Structure**: `net.malevy.chatserver`

## Development Notes

- Uses Maven wrapper (`mvnw`/`mvnw.cmd`) - no need to install Maven separately
- Lombok is configured for annotation processing
- Spring Boot DevTools enables automatic restart during development
- Application name is configured as "server" in `application.properties`