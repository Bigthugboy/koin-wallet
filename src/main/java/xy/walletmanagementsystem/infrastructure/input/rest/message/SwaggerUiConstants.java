package xy.walletmanagementsystem.infrastructure.input.rest.message;

public class SwaggerUiConstants {

    // Auth Controller
    public static final String AUTH_TAG_NAME = "Authentication";
    public static final String AUTH_TAG_DESCRIPTION = "Endpoints for user authentication and authorization";
    
    public static final String SIGNUP_SUMMARY = "Register a new user";
    public static final String SIGNUP_DESCRIPTION = "Creates a new user account and automatically initializes a wallet.";
    
    public static final String LOGIN_SUMMARY = "Login user";
    public static final String LOGIN_DESCRIPTION = "Authenticates a user and returns access and refresh tokens.";
    
    public static final String FORGOT_PASSWORD_SUMMARY = "Initiate password recovery";
    public static final String FORGOT_PASSWORD_DESCRIPTION = "Sends a password reset OTP to the user's registered email.";
    
    public static final String RESET_PASSWORD_SUMMARY = "Reset password using OTP";
    public static final String RESET_PASSWORD_DESCRIPTION = "Resets the user's password after verifying the provided OTP.";
    
    public static final String RESEND_OTP_SUMMARY = "Resend OTP";
    public static final String RESEND_OTP_DESCRIPTION = "Generates and sends a new OTP for the specified type (Signup/Password Reset).";

    public static final String LOGOUT_USER = "Logout user";
    public static final String LOGOUT_USER_DESCRIPTION = "Invalidates the user's current session and tokens.";

    // KYC Controller
    public static final String KYC_TAG_NAME = "KYC Management";
    public static final String KYC_TAG_DESCRIPTION = "Endpoints for user KYC verification";
    
    public static final String SUBMIT_KYC_SUMMARY = "Submit KYC details (BVN/NIN)";
    public static final String SUBMIT_KYC_DESCRIPTION = "Submits the user's BVN and NIN for verification. Limit: One submission per user.";
    
    public static final String GET_KYC_STATUS_SUMMARY = "Get KYC verification status";
    public static final String GET_KYC_STATUS_DESCRIPTION = "Retrieves the current status of the user's KYC verification (Pending/Verified/Rejected).";

    // Loan Controller
    public static final String LOAN_TAG_NAME = "Loan Management";
    public static final String LOAN_TAG_DESCRIPTION = "Endpoints for loan application and processing";
    
    public static final String APPLY_LOAN_SUMMARY = "Apply for a loan";
    public static final String APPLY_LOAN_DESCRIPTION = "Applies for a loan. Amount must not exceed 3 times the current wallet balance.";
    
    public static final String APPROVE_LOAN_SUMMARY = "Approve a loan (Admin only simulation)";
    public static final String APPROVE_LOAN_DESCRIPTION = "Simulates administrative approval of a pending loan application.";
    
    public static final String DISBURSE_LOAN_SUMMARY = "Disburse an approved loan";
    public static final String DISBURSE_LOAN_DESCRIPTION = "Credits the loan amount to the user's wallet after approval.";
    
    public static final String REPAY_LOAN_SUMMARY = "Repay a loan";
    public static final String REPAY_LOAN_DESCRIPTION = "Deducts the repayment amount from the user's wallet balance.";
    
    public static final String GET_LOAN_DETAILS_SUMMARY = "View loan details";
    public static final String GET_LOAN_DETAILS_DESCRIPTION = "Retrieves comprehensive details about a specific loan by ID.";
    
    public static final String GET_MY_LOANS_SUMMARY = "List all loans for the current user";
    public static final String GET_MY_LOANS_DESCRIPTION = "Retrieves a list of all loans applied for by the authenticated user.";

    public static final String GET_ALL_LOANS_SUMMARY = "List all loans (Admin view simulation)";
    public static final String GET_ALL_LOANS_DESCRIPTION = "Retrieves a global list of all loans in the system. for admin view.";

    // User Controller
    public static final String USER_TAG_NAME = "User Management";
    public static final String USER_TAG_DESCRIPTION = "Endpoints for managing user profiles";
    
    public static final String GET_PROFILE_SUMMARY = "Get current user profile";
    public static final String GET_PROFILE_DESCRIPTION = "Retrieves the profile details of the currently authenticated user.";
    
    public static final String UPDATE_PROFILE_SUMMARY = "Update user profile";
    public static final String UPDATE_PROFILE_DESCRIPTION = "Updates the authenticated user's profile information (Full Name, Phone Number).";

    // Wallet Controller
    public static final String WALLET_TAG_NAME = "Wallet Management";
    public static final String WALLET_TAG_DESCRIPTION = "Endpoints for wallet operations and transaction history";
    
    public static final String FUND_WALLET_SUMMARY = "Fund wallet (Simulate payment)";
    public static final String FUND_WALLET_DESCRIPTION = "Simulates funding the wallet. In a real scenario, this would be triggered by a payment webhook.";
    
    public static final String GET_BALANCE_SUMMARY = "Check wallet balance";
    public static final String GET_BALANCE_DESCRIPTION = "Retrieves the current balance and status of the user's wallet.";
    
    public static final String GET_TRANSACTIONS_SUMMARY = "View transaction history";
    public static final String GET_TRANSACTIONS_DESCRIPTION = "Retrieves a history of all financial transactions for the authenticated user.";

    public static final String CREATE_WALLET_SUMMARY = "Create wallet for user";
    public static final String CREATE_WALLET_DESCRIPTION = "Initializes a wallet for the user. Use to create a wallet for existing users without one";
    public static final String INITIALIZE_PAYMENT = "Initialize payment";
    public static final String INITILIZE_PAYMENT_DESCRIPTION = "Creates a PENDING transaction and returns a Paystack authorization URL to redirect the user to for payment";

    // Transaction Controller
    public static final String TRANSACTION_TAG_NAME = "Transaction History";
    public static final String TRANSACTION_TAG_DESCRIPTION = "Endpoints for listing and fetching transactions";
    
    public static final String GET_ALL_TRANSACTIONS_SUMMARY = "List all transactions (Admin view simulation)";
    public static final String GET_ALL_TRANSACTIONS_DESCRIPTION = "Retrieves a global list of all transactions in the system. Simulated admin view.";
    
    public static final String GET_TRANSACTION_SUMMARY = "Fetch a single transaction by ID";
    public static final String GET_TRANSACTION_DESCRIPTION = "Retrieves details of a specific transaction by its unique ID.";

    // Webhook Controller
    public static final String WEBHOOK_TAG_NAME = "Webhooks";
    public static final String WEBHOOK_TAG_DESCRIPTION = "Endpoints for external service callbacks";
    
    public static final String PAYSTACK_WEBHOOK_SUMMARY = "Receive Paystack payment confirmation";
    public static final String PAYSTACK_WEBHOOK_DESCRIPTION = "Awaits payment notification from Paystack to update wallet balance.";


}
