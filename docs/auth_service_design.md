# Auth Service Design

This document details the request flows for the Authentication Use Cases.

## User Registration Flow

```mermaid
sequenceDiagram
    actor User
    participant AuthController
    participant AuthUseCase (Service)
    participant PasswordEncoder
    participant UserOutPutPort
    participant WalletUseCase
    participant OtpUseCase
    participant EmailAdapter

    User->>AuthController: POST /api/v1/auth/signup
    AuthController->>AuthUseCase: signup(user, password)
    AuthUseCase->>UserOutPutPort: existsByEmail / existsByPhoneNumber
    UserOutPutPort-->>AuthUseCase: false
    AuthUseCase->>PasswordEncoder: encode(password)
    AuthUseCase->>UserOutPutPort: save(User)
    UserOutPutPort-->>AuthUseCase: Saved User
    AuthUseCase->>WalletUseCase: createWallet(userId)
    AuthUseCase->>OtpUseCase: generateAndSendOtp(email)
    OtpUseCase->>EmailAdapter: sendRegistrationOtp(email, OTP)
    AuthUseCase-->>AuthController: Saved User
    AuthController-->>User: 200 OK + UserResponse
```

## User Login Flow

```mermaid
sequenceDiagram
    actor User
    participant AuthController
    participant AuthUseCase (Service)
    participant AuthenticationManager
    participant JwtService
    participant UserOutPutPort

    User->>AuthController: POST /api/v1/auth/login
    AuthController->>AuthUseCase: login(email, password)
    AuthUseCase->>AuthenticationManager: authenticate(UsernamePasswordAuthenticationToken)
    AuthenticationManager-->>AuthUseCase: Authentication
    AuthUseCase->>UserOutPutPort: findByEmail(email)
    UserOutPutPort-->>AuthUseCase: User
    AuthUseCase->>JwtService: generateToken(User)
    JwtService-->>AuthUseCase: JWT Token String
    AuthUseCase-->>AuthController: AuthResponse (Token)
    AuthController-->>User: 200 OK + AuthResponse
```

## User Logout Flow

```mermaid
sequenceDiagram
    actor User
    participant AuthController
    participant AuthUseCase (Service)
    participant TokenBlackAdapter

    User->>AuthController: POST /api/v1/auth/logout (Bearer Token)
    AuthController->>AuthUseCase: logout(userId, token)
    AuthUseCase->>TokenBlackAdapter: blacklistToken(token)
    TokenBlackAdapter-->>AuthUseCase: Void
    AuthUseCase-->>AuthController: Success
    AuthController-->>User: 200 OK
```

## Password Recovery Flow

```mermaid
sequenceDiagram
    actor User
    participant AuthController
    participant AuthUseCase (Service)
    participant UserOutPutPort
    participant OtpUseCase
    participant PasswordEncoder

    %% Step 1: Request OTP
    User->>AuthController: POST /api/v1/auth/forgot-password?email=X
    AuthController->>AuthUseCase: forgetPassword(email)
    AuthUseCase->>UserOutPutPort: findByEmail(email)
    AuthUseCase->>OtpUseCase: generateAndSendOtp(email, FORGOT_PASSWORD)
    AuthUseCase-->>AuthController: Success
    AuthController-->>User: 200 OK (OTP Sent)

    %% Step 2: Reset Password
    User->>AuthController: POST /api/v1/auth/reset-password
    AuthController->>AuthUseCase: resetPassword(email, otp, newPassword)
    AuthUseCase->>OtpUseCase: validateOtp(email, otp, FORGOT_PASSWORD)
    AuthUseCase->>PasswordEncoder: encode(newPassword)
    AuthUseCase->>UserOutPutPort: save(Updated User)
    AuthUseCase-->>AuthController: Success
    AuthController-->>User: 200 OK
```

## Resend OTP Flow

```mermaid
sequenceDiagram
    actor User
    participant AuthController
    participant OtpUseCase (Service)
    participant OtpOutPutPort
    participant EmailAdapter

    User->>AuthController: POST /api/v1/auth/resend-otp?email=X&type=Y
    AuthController->>OtpUseCase: resendOtp(email, type)
    OtpUseCase->>OtpOutPutPort: deleteByEmailAndType(email, type)
    OtpUseCase->>OtpOutPutPort: save(New OtpDetails)
    OtpUseCase->>EmailAdapter: sendOtpEmail(email, newOtp)
    OtpUseCase-->>AuthController: Success
    AuthController-->>User: 200 OK
```
