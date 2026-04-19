# Wallet & Transaction Service Design

This document details the request flows for Wallet operations and Transactions.

## Wallet Funding Initialization

```mermaid
sequenceDiagram
    actor User
    participant WalletController
    participant WalletUseCase (Service)
    participant WalletOutPutPort
    participant PaystackService
    participant TransactionOutPutPort

    User->>WalletController: POST /api/v1/wallets/fund
    WalletController->>WalletUseCase: initializeFunding(userId, amount)
    WalletUseCase->>WalletOutPutPort: findByUserId(userId)
    WalletUseCase->>PaystackService: initializeTransaction(amount, email, reference)
    PaystackService-->>WalletUseCase: Authorization URL
    WalletUseCase->>TransactionOutPutPort: save(Transaction - PENDING)
    WalletUseCase-->>WalletController: Authorization URL
    WalletController-->>User: 200 OK + Auth URL
```

## Paystack Webhook Processing (Funding Confirmation)

```mermaid
sequenceDiagram
    participant Paystack
    participant WebhookController
    participant WebhookUseCase (Service)
    participant WalletUseCase
    participant WalletOutPutPort
    participant TransactionOutPutPort
    participant NotificationOutPutPort

    Paystack->>WebhookController: POST /api/v1/webhooks/paystack (Payload, Signature)
    WebhookController->>WebhookUseCase: processPaystackWebhook(payload, signature)
    WebhookUseCase->>WebhookUseCase: verifyHmacSignature(payload, signature)
    WebhookUseCase->>WebhookUseCase: parseEvent(payload)
    
    %% If success
    opt If Event == charge.success
        WebhookUseCase->>WalletUseCase: confirmFunding(reference, amount)
        WalletUseCase->>TransactionOutPutPort: findByReference(reference)
        WalletUseCase->>WalletOutPutPort: findByUserId(userId)
        WalletUseCase->>WalletOutPutPort: save(Updated Balance Wallet)
        WalletUseCase->>TransactionOutPutPort: save(Transaction - SUCCESSFUL)
        WalletUseCase->>NotificationOutPutPort: sendWalletNotification(email, amount)
    end
    
    %% If failure
    opt If Event == charge.failed or transfer.failed
        WebhookUseCase->>WalletUseCase: markTransactionTerminal(reference, FAILED)
        WalletUseCase->>TransactionOutPutPort: findByReference(reference)
        WalletUseCase->>TransactionOutPutPort: save(Transaction - FAILED)
    end

    WebhookUseCase-->>WebhookController: Void
    WebhookController-->>Paystack: 200 OK
```

## Fetching Transactions (All)

```mermaid
sequenceDiagram
    actor Admin
    participant TransactionController
    participant TransactionOutPutPort

    Admin->>TransactionController: GET /api/v1/transactions
    TransactionController->>TransactionOutPutPort: findAll()
    TransactionOutPutPort-->>TransactionController: List<Transaction>
    TransactionController-->>Admin: 200 OK + List<TransactionResponse>
```

## Fetching Single Transaction

```mermaid
sequenceDiagram
    actor Admin
    participant TransactionController
    participant TransactionOutPutPort

    Admin->>TransactionController: GET /api/v1/transactions/{id}
    TransactionController->>TransactionOutPutPort: findById(transactionId)
    TransactionOutPutPort-->>TransactionController: Transaction
    TransactionController-->>Admin: 200 OK + TransactionResponse
```

## Get Wallet Balance

```mermaid
sequenceDiagram
    actor User
    participant WalletController
    participant WalletUseCase (Service)
    participant WalletOutPutPort

    User->>WalletController: GET /api/v1/wallets/balance
    WalletController->>WalletUseCase: getWalletBalance(userId)
    WalletUseCase->>WalletOutPutPort: findByUserId(userId)
    WalletOutPutPort-->>WalletUseCase: Wallet
    WalletUseCase-->>WalletController: Wallet
    WalletController-->>User: 200 OK + WalletResponse
```

## Get User Transaction History

```mermaid
sequenceDiagram
    actor User
    participant WalletController
    participant WalletUseCase (Service)
    participant TransactionOutPutPort

    User->>WalletController: GET /api/v1/wallets/transactions
    WalletController->>WalletUseCase: getTransactionHistory(userId)
    WalletUseCase->>TransactionOutPutPort: findByUserId(userId)
    TransactionOutPutPort-->>WalletUseCase: List<Transaction>
    WalletUseCase-->>WalletController: List<Transaction>
    WalletController-->>User: 200 OK + List<TransactionResponse>
```
