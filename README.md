# KOINS Wallet & Loan Management System

A simple fintech backend simulation built with Java and Spring Boot. This system handles user onboarding, wallet management, loan processing, and transaction tracking.

## Features

- **Authentication**: Secure signup and login with JWT.
- **Wallet Management**: Automatic wallet creation on signup, balance tracking, and funding via Paystack integration (mocked).
- **Loan Processing**: Loan application with a 3x wallet balance constraint, approval workflow, disbursement, and repayment.
- **KYC Management**: Separate handling for BVN and NIN verification.
- **Transaction History**: Comprehensive logs for all financial operations.
- **Messaging**: Asynchronous notifications using Kafka for loan approvals and payments.
- **Scheduled Jobs**: Automated loan repayment reminders and defaulting of overdue loans.
- **API Documentation**: Integrated Swagger/OpenAPI documentation.

## Tech Stack

- **Java 17**
- **Spring Boot 3.x/4.x**
- **Spring Security & JWT**
- **Spring Data JPA**
- **PostgreSQL**
- **Apache Kafka**
- **MapStruct & Lombok**
- **Docker & Docker Compose**

## Setup Instructions

### Prerequisites
- JDK 17+
- Maven
- Docker & Docker Compose

### Running with Docker (Recommended)
1. Clone the repository.
2. Run the following command in the root directory:
   ```bash
   docker-compose up --build
   ```
3. The application will be available at `http://localhost:8080`.

### Running Locally
1. Start a PostgreSQL instance and a Kafka broker.
2. Update `src/main/resources/application.properties` with your local credentials.
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## API Documentation
Once the application is running, you can access the Swagger UI at:
`http://localhost:8080/swagger-ui/index.html`

## Environment Variables
- `DB_URL`: JDBC URL for PostgreSQL.
- `DB_USERNAME`: Database username.
- `DB_PASSWORD`: Database password.
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Kafka broker addresses.

## Key Design Decisions
- **Hexagonal Architecture**: Clean separation between domain logic and infrastructure adapters.
- **JWT Authentication**: Stateless security with role-based access control.
- **Validation**: Strict input validation using Jakarta Validation.
- **Exception Handling**: Centralized global exception handler for consistent API responses.

## License
MIT
