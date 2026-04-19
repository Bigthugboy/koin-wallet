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

## Architecture & System Design

The system follows a strict **Hexagonal Architecture** (Ports and Adapters) pattern. To showcase the intricate request flows, sequence diagrams and system architecture graphs have been documented.

Please view the comprehensive design documentation here:
- [System Architecture](docs/system_architecture.md)
- [Auth Service Design](docs/auth_service_design.md)
- [User & KYC Service Design](docs/user_kyc_service_design.md)
- [Wallet & Transaction Service Design](docs/wallet_transaction_service_design.md)
- [Loan Service Design](docs/loan_service_design.md)

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

## Database Scripts & Test Data
The application relies on Hibernate's `ddl-auto=update` to manage the schema during runtime, but explicit database scripts are provided for manual setup and testing:
- **Schema Script**: [`database_scripts/schema.sql`](database_scripts/schema.sql)
- **Sample Test Data**: [`database_scripts/data.sql`](database_scripts/data.sql)

To load the sample data, run the `data.sql` script against your PostgreSQL instance. It includes an Admin user and a Standard user (both with password: `password123`), pre-funded wallets, KYC records, and sample loan transactions.

## API Documentation
Once the application is running, you can access the Swagger UI at:
`http://localhost:8080/swagger-ui/index.html`

## Environment Variables
The application requires the following environment variables. Default values are provided in `application.properties` for local development.

### Database
- `DB_URL`: JDBC URL for PostgreSQL.
- `DB_USERNAME`: Database username.
- `DB_PASSWORD`: Database password.

### Security / JWT
- `JWT_SECRET`: Secret key used for signing JWTs (minimum 256 bits).
- `JWT_ACCESS_EXPIRATION`: Access token expiration time in milliseconds.
- `JWT_REFRESH_EXPIRATION`: Refresh token expiration time in milliseconds.

### Messaging
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker addresses (e.g., `localhost:9092`).

### External APIs
- `PAYSTACK_SECRET_KEY`: Your Paystack Secret Key for initializing funding and verifying webhooks.

### Email Configuration (SMTP)
- `MAIL_HOST`: SMTP server host (e.g., `smtp.gmail.com`).
- `MAIL_PORT`: SMTP server port.
- `MAIL_USERNAME`: SMTP authenticated email address.
- `MAIL_PASSWORD`: SMTP app password.

## Key Design Decisions
- **Hexagonal Architecture**: Clean separation between domain logic and infrastructure adapters.
- **JWT Authentication**: Stateless security with role-based access control.
- **Validation**: Strict input validation using Jakarta Validation.
- **Exception Handling**: Centralized global exception handler for consistent API responses.
