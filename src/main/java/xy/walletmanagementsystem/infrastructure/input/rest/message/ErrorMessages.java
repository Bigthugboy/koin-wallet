package xy.walletmanagementsystem.infrastructure.input.rest.message;

public class ErrorMessages {
    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later.";
    public static final String FORBIDDEN = "You do not have permission to access this resource.";
    public static final String INVALID_INPUT_ERROR_MESSAGE = "Invalid input. Please check your request and try again.";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String KYC_ALREADY_SUBMITTED = "KYC details have already been submitted for this user.";
    public static final String KYC_NOT_FOUND = "KYC details not found for this user.";
    public static final String WALLET_NOT_FOUND = "Wallet not found for this user.";
    public static final String USER_ID_IS_REQUIRED = "User ID is required.";
    public static final String LOAN_ID_IS_REQUIRED = "Loan ID is required.";
    public static final String LOAN_NOT_FOUND = "Loan not found.";
    public static final String REPAYMENT_AMOUNT_MUST_BE_POSITIVE = "Repayment amount must be a positive value.";
    public static final String LOAN_NOT_IN_PAYMENT_STATUS = "Loan must be in disbursed status to make a repayment.";
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds in wallet to make the repayment.";
    public static final String USER_ALREADY_HAS_WALLET = "User already has a wallet.";
    public static final String AMOUNT_MUST_BE_GREATER_THAN_ZERO = "Amount must be greater than zero.";
    public static final String USER_EMAIL_ALREADY_EXISTS = "A user with this email already exists.";
}
