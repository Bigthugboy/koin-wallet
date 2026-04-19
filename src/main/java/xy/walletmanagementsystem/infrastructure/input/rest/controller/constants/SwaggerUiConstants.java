package xy.walletmanagementsystem.infrastructure.input.rest.controller.constants;

public class SwaggerUiConstants {

    // Auth Controller
    public static final String AUTH_TAG_NAME = "Authentication";
    public static final String AUTH_TAG_DESCRIPTION = "Endpoints for user authentication and authorization";
    public static final String SIGNUP_SUMMARY = "Register a new user";
    public static final String LOGIN_SUMMARY = "Login user";
    public static final String FORGOT_PASSWORD_SUMMARY = "Initiate password recovery";
    public static final String RESET_PASSWORD_SUMMARY = "Reset password using OTP";
    public static final String RESEND_OTP_SUMMARY = "Resend OTP";

    // KYC Controller
    public static final String KYC_TAG_NAME = "KYC Management";
    public static final String KYC_TAG_DESCRIPTION = "Endpoints for user KYC verification";
    public static final String SUBMIT_KYC_SUMMARY = "Submit KYC details (BVN/NIN)";
    public static final String GET_KYC_STATUS_SUMMARY = "Get KYC verification status";

    // Loan Controller
    public static final String LOAN_TAG_NAME = "Loan Management";
    public static final String LOAN_TAG_DESCRIPTION = "Endpoints for loan application and processing";
    public static final String APPLY_LOAN_SUMMARY = "Apply for a loan";
    public static final String APPROVE_LOAN_SUMMARY = "Approve a loan (Admin only simulation)";
    public static final String DISBURSE_LOAN_SUMMARY = "Disburse an approved loan";
    public static final String REPAY_LOAN_SUMMARY = "Repay a loan";
    public static final String GET_LOAN_DETAILS_SUMMARY = "View loan details";
    public static final String GET_MY_LOANS_SUMMARY = "List all loans for the current user";

    // User Controller
    public static final String USER_TAG_NAME = "User Management";
    public static final String USER_TAG_DESCRIPTION = "Endpoints for managing user profiles";
    public static final String GET_PROFILE_SUMMARY = "Get current user profile";
    public static final String UPDATE_PROFILE_SUMMARY = "Update user profile";

    // Wallet Controller
    public static final String WALLET_TAG_NAME = "Wallet Management";
    public static final String WALLET_TAG_DESCRIPTION = "Endpoints for wallet operations and transaction history";
    public static final String FUND_WALLET_SUMMARY = "Fund wallet (Simulate payment)";
    public static final String GET_BALANCE_SUMMARY = "Check wallet balance";
    public static final String GET_TRANSACTIONS_SUMMARY = "View transaction history";

    // Transaction Controller
    public static final String TRANSACTION_TAG_NAME = "Transaction History";
    public static final String TRANSACTION_TAG_DESCRIPTION = "Endpoints for listing and fetching transactions";
    public static final String GET_ALL_TRANSACTIONS_SUMMARY = "List all transactions (Admin view simulation)";
    public static final String GET_TRANSACTION_SUMMARY = "Fetch a single transaction by ID";

    // Webhook Controller
    public static final String WEBHOOK_TAG_NAME = "Webhooks";
    public static final String WEBHOOK_TAG_DESCRIPTION = "Endpoints for external service callbacks";
    public static final String PAYSTACK_WEBHOOK_SUMMARY = "Receive Paystack payment confirmation";
}
