# MailFlow

## Overview

MailFlow is a simplified email marketing platform built with a microservice architecture. It focuses on campaign management and contact organization, allowing users to create targeted email campaigns that are automatically triggered when contacts are tagged with specific tags.

## Tech Stack

- **Java**: Java 17+
- **Spring Boot 3**: For creating microservices
- **Spring Cloud**: For service discovery and configuration
- **PostgreSQL**: For persistent data storage
- **Kafka**: For event-driven communication between services
- **Keycloak**: For authentication and authorization
- **Docker**: For containerization
- **Maven**: For dependency management and build

## Architecture

MailFlow is built using a microservice architecture with the following components:

### API Layer
- **API Gateway**: Routes requests to appropriate services
- **Auth Service**: Handles authentication and user management

### Core Services
- **Contact Service**: Manages contacts and their tags
- **Campaign Service**: Manages email campaigns and their triggers
- **Template Service**: Manages email templates
- **Email Service**: Handles email sending and tracking
- **Discovery Server**: Eureka service for service discovery

### Communication Patterns
- **Synchronous**: REST API calls between services using OpenFeign
- **Asynchronous**: Event-driven communication using Kafka

## Services

### Contact Service
- Manages contact information and tagging
- Endpoints for CRUD operations on contacts
- Search capabilities for contacts by name, email, and tags

### Campaign Service
- Creates and manages email campaigns
- Associates campaigns with trigger tags and templates
- Activates/deactivates campaigns
- Handles campaign triggering logic

### Template Service
- Manages email templates
- Renders templates with contact data
- Supports variables for personalization

### Email Service
- Sends emails to contacts
- Tracks email opens and clicks
- Provides analytics on email performance

### Auth Service
- Handles user authentication and authorization
- Integrates with Keycloak for identity management

## Application Flow

1. User creates contacts with specific tags
2. User creates campaigns with a trigger tag and an email template
3. EmailService continuously monitors contacts for matching tags
4. When a contact's tags match a campaign's trigger tag:
    - Renders the email template with contact data
    - Sends the email to the contact
    - Tracks email opens and clicks

## Setup Instructions

### Prerequisites
- Java 17+
- Maven
- Docker and Docker Compose
- PostgreSQL
- Kafka
- Keycloak

### Configuration

Each service has its own configuration file in the `resources` directory, typically named `application.yml` or service-specific YAML files. Here are some key configuration files:

- `auth-service.yml`: Keycloak configuration
- `contact-service.yml`: Database configuration for contacts
- `discovery-server.yml`: Eureka server configuration
- `application.yml`: Common configuration for all services

### Build and Run

1. Clone the repository
```
git clone https://github.com/HMZElidrissi/mailflow.git
cd mailflow
```

2. Build the project
```
mvn clean install
```

3. Start the infrastructure services
```
docker-compose up -d
```

4. Start the services in order:
    - Discovery Server
    - Auth Service
    - Contact Service
    - Template Service
    - Campaign Service
    - Email Service

```
cd discovery-server
mvn spring-boot:run

# In separate terminals, start each service
cd auth-service
mvn spring-boot:run

cd contact-service
mvn spring-boot:run

# Continue for each service
```

