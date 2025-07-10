# Chat Application

A real-time chat application built with Spring Boot and React to explore WebSocket communication.

## Overview

This project demonstrates WebSocket implementation using:
- **Backend**: Spring Boot 3.5.3 with Spring WebSocket
- **Frontend**: React (to be implemented)
- **Communication**: WebSocket for real-time messaging

## Project Structure

```
chat/
├── server/          # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/net/malevy/chatserver/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
└── README.md
```

## Getting Started

### Prerequisites

- Java 21
- Maven (or use included wrapper)
- Node.js and npm (for React frontend)

### Running the Backend

```bash
cd server
./mvnw spring-boot:run
```

The server will start on `http://localhost:8080`

### Running Tests

```bash
cd server
./mvnw test
```

## Features (Planned)

- [ ] Real-time messaging via WebSocket
- [ ] Multiple chat rooms
- [ ] User authentication
- [ ] Message history
- [ ] Typing indicators
- [ ] Online user presence

## Technology Stack

### Backend
- Spring Boot 3.5.3
- Spring WebSocket
- Java 21
- Maven
- Lombok

### Frontend (To be implemented)
- React
- WebSocket client
- Modern CSS/styling framework

## Development

This project uses Spring Boot DevTools for hot reload during development. Changes to Java files will automatically restart the server.

## Learning Goals

- Understand WebSocket protocol and implementation
- Explore real-time communication patterns
- Practice Spring Boot WebSocket configuration
- Implement React WebSocket client
- Handle connection management and error scenarios