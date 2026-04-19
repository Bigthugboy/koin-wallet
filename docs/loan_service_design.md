# Loan Service Design

This document details the request flows for Loan operations, from Application to Disbursement, Repayment, and Scheduled jobs.

## Loan Application

```mermaid
sequenceDiagram
    actor User
    participant LoanController
    participant LoanUseCase (Service)
    participant KycOutPutPort
    participant WalletOutPutPort
    participant LoanOutPutPort
    participant NotificationOutPutPort

    User->>LoanController: POST /api/v1/loans/apply
    LoanController->>LoanUseCase: applyForLoan(userId, amount, duration)
    LoanUseCase->>KycOutPutPort: findByUserId(userId)
    Note over LoanUseCase, KycOutPutPort: Checks if KycVerificationStatus is VERIFIED
    LoanUseCase->>WalletOutPutPort: findByUserId(userId)
    Note over LoanUseCase, WalletOutPutPort: Validates that amount <= 3 * Wallet Balance
    LoanUseCase->>LoanOutPutPort: save(LoanEntity - PENDING)
    LoanUseCase->>NotificationOutPutPort: sendLoanNotification(email, message)
    LoanUseCase-->>LoanController: LoanResponse
    LoanController-->>User: 200 OK + LoanResponse
```

## Loan Approval & Disbursement

```mermaid
sequenceDiagram
    actor Admin
    participant LoanController
    participant LoanUseCase (Service)
    participant LoanOutPutPort
    participant WalletOutPutPort
    participant TransactionOutPutPort
    participant NotificationOutPutPort

    %% Approval
    Admin->>LoanController: POST /api/v1/loans/{id}/approve
    LoanController->>LoanUseCase: approveLoan(loanId)
    LoanUseCase->>LoanOutPutPort: save(LoanEntity - APPROVED)
    LoanUseCase->>NotificationOutPutPort: sendLoanNotification(email)
    LoanUseCase-->>LoanController: LoanResponse

    %% Disbursement
    Admin->>LoanController: POST /api/v1/loans/{id}/disburse
    LoanController->>LoanUseCase: disburseLoan(loanId)
    LoanUseCase->>LoanOutPutPort: findById(loanId) (Must be APPROVED)
    LoanUseCase->>WalletOutPutPort: findByUserId(userId)
    LoanUseCase->>WalletOutPutPort: save(Wallet with Increased Balance)
    LoanUseCase->>LoanOutPutPort: save(LoanEntity - DISBURSED)
    LoanUseCase->>TransactionOutPutPort: save(Transaction - LOAN_DISBURSEMENT)
    LoanUseCase->>NotificationOutPutPort: sendLoanNotification(email)
    LoanUseCase-->>LoanController: LoanResponse
```

## Loan Repayment

```mermaid
sequenceDiagram
    actor User
    participant LoanController
    participant LoanUseCase (Service)
    participant LoanOutPutPort
    participant WalletOutPutPort
    participant TransactionOutPutPort
    participant NotificationOutPutPort

    User->>LoanController: POST /api/v1/loans/{id}/repay
    LoanController->>LoanUseCase: repayLoan(loanId, amount)
    LoanUseCase->>LoanOutPutPort: findById(loanId)
    Note over LoanUseCase, LoanOutPutPort: Validates Loan is DISBURSED/PARTIALLY_REPAID
    LoanUseCase->>WalletOutPutPort: findByUserId(userId)
    Note over LoanUseCase, WalletOutPutPort: Validates Wallet Balance >= Repayment Amount
    LoanUseCase->>WalletOutPutPort: save(Wallet with Decreased Balance)
    LoanUseCase->>LoanOutPutPort: save(LoanEntity - REPAID / PARTIALLY_REPAID)
    LoanUseCase->>TransactionOutPutPort: save(Transaction - REPAYMENT)
    LoanUseCase->>NotificationOutPutPort: sendLoanNotification(email, Remaining Balance)
    LoanUseCase-->>LoanController: LoanResponse
    LoanController-->>User: 200 OK
```

## Scheduled Jobs (Loan Reminders & Overdue Marking)

```mermaid
sequenceDiagram
    participant Cron (Spring Scheduler)
    participant LoanScheduler
    participant LoanOutPutPort
    participant UserOutPutPort
    participant NotificationOutPutPort

    %% 8 AM Daily: Send Reminders
    Cron->>LoanScheduler: trigger 8 AM (sendRepaymentReminders)
    LoanScheduler->>LoanOutPutPort: findAll() (Filter DISBURSED)
    loop For each loan due in 3 days
        LoanScheduler->>UserOutPutPort: findById(userId)
        LoanScheduler->>NotificationOutPutPort: sendLoanNotification(email, reminder)
    end

    %% Midnight Daily: Mark Defaulted
    Cron->>LoanScheduler: trigger 12 AM (markOverdueLoans)
    LoanScheduler->>LoanOutPutPort: findAll() (Filter DISBURSED)
    loop For each loan past due date
        LoanScheduler->>LoanOutPutPort: save(LoanEntity - DEFAULTED)
    end
```

## Get Loan Details

```mermaid
sequenceDiagram
    actor User
    participant LoanController
    participant LoanUseCase (Service)
    participant LoanOutPutPort

    User->>LoanController: GET /api/v1/loans/{id}
    LoanController->>LoanUseCase: getLoanDetails(loanId)
    LoanUseCase->>LoanOutPutPort: findById(loanId)
    LoanOutPutPort-->>LoanUseCase: Loan
    LoanUseCase-->>LoanController: Loan
    LoanController-->>User: 200 OK + LoanResponse
```

## Get My Loans

```mermaid
sequenceDiagram
    actor User
    participant LoanController
    participant LoanUseCase (Service)
    participant LoanOutPutPort

    User->>LoanController: GET /api/v1/loans/my-loans
    LoanController->>LoanUseCase: getAllLoansForUser(userId)
    LoanUseCase->>LoanOutPutPort: findByUserId(userId)
    LoanOutPutPort-->>LoanUseCase: List<Loan>
    LoanUseCase-->>LoanController: List<Loan>
    LoanController-->>User: 200 OK + List<LoanResponse>
```

## List All Loans (Admin)

```mermaid
sequenceDiagram
    actor Admin
    participant LoanController
    participant LoanUseCase (Service)
    participant LoanOutPutPort

    Admin->>LoanController: GET /api/v1/loans
    LoanController->>LoanUseCase: listAllLoans()
    LoanUseCase->>LoanOutPutPort: findAll()
    LoanOutPutPort-->>LoanUseCase: List<Loan>
    LoanUseCase-->>LoanController: List<Loan>
    LoanController-->>Admin: 200 OK + List<LoanResponse>
```
