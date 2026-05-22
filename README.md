# Payment Wallet System

A **distributed microservices-based Payment Wallet System** built using Spring Boot, demonstrating real-world backend architecture, resilience patterns, and transaction management.

**Live Demo** | [Eureka dashboard](http://localhost:8761) (after running)

## 📋 Project Overview

This project is a **fully functional Payment Wallet System** with multiple microservices communicating via REST and Feign Clients. It demonstrates production-grade practices including distributed transactions, resilience, security, and containerization.

## ✨ Key Features

- **User Management** (Registration, Login, JWT)
- **Wallet Management** (Add Money, Balance Check)
- **P2P Money Transfer** with Saga Pattern
- **Distributed Locking** using Redis
- **Centralized Authentication** at API Gateway
- **Resilience Patterns** (Circuit Breaker + Retry + Fallback)
- **Rate Limiting** at Gateway level
- **Real-time Notifications** (via Notification Service)
- **Dockerized** deployment with Docker Compose

## 🛠 Tech Stack

| Layer              | Technology |
|--------------------|----------|
| Framework          | Spring Boot 3.5.9 |
| Service Discovery  | Eureka Server |
| API Gateway        | Spring Cloud Gateway |
| Inter-service Call | OpenFeign |
| Database           | H2 (In-memory) |
| Cache & Locking    | Redis |
| Resilience         | Resilience4j (Circuit Breaker, Retry, Rate Limiter) |
| Security           | JWT (Centralized at Gateway) |
| Containerization   | Docker + Docker Compose |
| Monitoring         | Spring Boot Actuator |
| Build Tool         | Maven (Multi-module) |

## 🏗 Architecture
+-----------------+
|   API Gateway   |  ← JWT Auth + Rate Limiting
+-----------------+
|
+------------------+------------------+
|                  |                  |
User Service     Wallet Service     Transaction Service
|                  |                  |
Notification Service     Redis (Cache + Distributed Lock)


**Design Patterns Used**:
- Choreography-based Saga Pattern
- Circuit Breaker & Retry
- Centralized Authentication
- Eventual Consistency

## 🚀 How to Run

### Using Docker Compose (Recommended)

```bash
# Clone the repo
git clone https://github.com/Mavishnu-KJ/payment-wallet-system.git
cd payment-wallet-system

# Build and run all services
mvn clean package -DskipTests
docker compose up --build
```

## Access Points

- Eureka Dashboard: http://localhost:8761
- Swagger UI : 
    - user-service : http://localhost:8081/swagger-ui/index.html
    - wallet-service : http://localhost:8082/swagger-ui/index.html
    - transaction-service : http://localhost:8083/swagger-ui/index.html
    - notification-service : http://localhost:8084/swagger-ui/index.html

- API Gateway: http://localhost:8080
    - Can call service APIs via API gateway as well
    - Examples (via restclient or postman): 
        - http://localhost:8080/api/users/register
        - http://localhost:8080/api/users/login
        - http://localhost:8080/api/wallets/me/addMoney
        - http://localhost:8080/api/transactions/me/transfer

## Services Ports

- Eureka Server - 8761
- API Gateway - 8080
- User Service - 8081
- Wallet Service - 8082
- Transaction Service - 8083
- Notification Service - 8084

## 📌 Important Endpoints

- POST /api/users/register
- POST /api/users/login
- POST /api/wallets/me/add-money
- POST /api/transactions/me/transfer

## 🧠 Key Learnings & Highlights

- => Implemented Choreography Saga Pattern for distributed transactions
- => Used Redis Distributed Locking to handle concurrency
- => Centralized JWT Authentication at API Gateway
- => Applied Resilience4j (Circuit Breaker, Retry, Rate Limiter)
- => Proper Docker multi-stage builds and profiles (dev & docker)
- => Clean separation of concerns using Feign Clients

## 📈 Future Enhancements

- => Integration with real payment gateway (Razorpay/Stripe)
- => Kafka for event-driven architecture
- => Distributed Tracing with Zipkin
- => Spring Cloud Config Server
- => Database migration to PostgreSQL
- => Role-based access control (ADMIN role)

## Author

- [Mavishnu KJ](http://www.linkedin.com/in/mavishnu-kj)

