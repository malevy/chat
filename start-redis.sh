#!/bin/bash

# Redis Docker Container Startup Script
# This script starts a Redis container for local development

CONTAINER_NAME="chat-redis"
REDIS_PORT="6379"
REDIS_PASSWORD=""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting Redis Docker container for chat application...${NC}"

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Stop and remove existing container if it exists
if docker ps -a --format 'table {{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo -e "${YELLOW}Stopping existing Redis container...${NC}"
    docker stop ${CONTAINER_NAME} >/dev/null 2>&1
    docker rm ${CONTAINER_NAME} >/dev/null 2>&1
fi

# Start Redis container
echo -e "${GREEN}Starting new Redis container...${NC}"
docker run -d \
  --name ${CONTAINER_NAME} \
  -p ${REDIS_PORT}:6379 \
  redis:7 \
  redis-server --appendonly yes

# Check if container started successfully
if docker ps --format 'table {{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo -e "${GREEN}✓ Redis container started successfully!${NC}"
    echo -e "${GREEN}✓ Container name: ${CONTAINER_NAME}${NC}"
    echo -e "${GREEN}✓ Port: ${REDIS_PORT}${NC}"
    echo -e "${GREEN}✓ Data persistence: enabled${NC}"
    echo ""
    echo -e "${YELLOW}Connection details:${NC}"
    echo -e "  Host: localhost"
    echo -e "  Port: ${REDIS_PORT}"
    echo -e "  Password: (none)"
    echo ""
    echo -e "${YELLOW}Useful commands:${NC}"
    echo -e "  Stop Redis:    docker stop ${CONTAINER_NAME}"
    echo -e "  Start Redis:   docker start ${CONTAINER_NAME}"
    echo -e "  Redis CLI:     docker exec -it ${CONTAINER_NAME} redis-cli"
    echo -e "  View logs:     docker logs ${CONTAINER_NAME}"
    echo -e "  Remove:        docker rm ${CONTAINER_NAME}"
else
    echo -e "${RED}✗ Failed to start Redis container${NC}"
    exit 1
fi