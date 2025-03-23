#!/bin/bash

echo "Building all microservices..."
 mvn clean install -DskipTest

echo "Building Docker images..."

docker build -t mailflow/config-server:latest ./config-server/
docker build -t mailflow/discovery-server:latest ./discovery-server/
docker build -t mailflow/api-gateway:latest ./api-gateway/
docker build -t mailflow/auth-service:latest ./auth-service/
docker build -t mailflow/contact-service:latest ./contact-service/
docker build -t mailflow/campaign-service:latest ./campaign-service/
docker build -t mailflow/template-service:latest ./template-service/
docker build -t mailflow/email-service:latest ./email-service/

docker-compose up -d