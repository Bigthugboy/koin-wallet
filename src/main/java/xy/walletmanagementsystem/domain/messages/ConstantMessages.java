package xy.walletmanagementsystem.domain.messages;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstantMessages {
    public static final String LOAN_DISBURSED = "Your loan has been disbursed to your wallet.";
    public static final String LOAN_APPROVED = "Your loan has been approved.";
    public static final String LOAN_REPAYMENT_SUCCESS = "Loan repayment of %s NGN was successful. Remaining balance is %s NGN.";
    public static final String INSTALLMENT_TEMPLATE = "[{\"installment\": %d, \"dueDate\": \"%s\", \"amountDue\": %s}]";
    public static  final String LOAN_APPLICATION = "Your loan application has been submitted and is pending approval.";
    public static final String REGISTRATION_SUCCESSFUL = "Registration successfully";
public static final String LOGIN_SUCCESSFUL = "Login successful";
public static final String OTP_SENT = "OTP sent to your email";
public static final String WELCOME_MESSAGE = "Welcome to Koin Wallet";
    public static final String ACCOUNT_CREATED_SUCCESS = "Hi %s, your account has been created successfully. as you join and fund your account may you be blessed. ";
    public static final String PASSWORD_RESET_SUCCESSFULLY = "Password reset successfully";
    public static final String PASSWORD_RESET_MESSAGE ="Your password has been changed successfully.";
    public static final String BEARER = "Bearer";
    public static final String PASSWORD_RESET_OTP= "Password Reset OTP";
    public static final String REGISTRATION_OTP = "Registration OTP";
    public static final String  EMAIL_CHANGE_OTP =   "Email Change OTP";
    public static final String RESEND_OTP="Resend OTP";
    public static final String   OTP_CODE =   "OTP Code";
    public static final String OTP_BODY = "Your OTP is %s. It expires in 10 minutes.";
    public static final String PROFILE_RETRIEVED =  "Profile retrieved successfully";
    public static final String PROFILE_UPDATED = "Profile updated successfully";
    public static final String PAYSTACK_SIGNATURE="x-paystack-signature";
    public static final String WEBHOOK_PROCESSED="Webhook processed";
    public static final String WEBHOOK_ERROR_ACKNOWLEDGED="Error acknowledged";
    public static final String WEBHOOK_CHARGE_SUCCESS ="charge.success";
    public static final String WEBHOOK_CHARGE_FAILED ="charge.failed";
    public static final String WEBHOOK_TRANSFER_SUCCESS= "transfer.success";
    public static final String WEBHOOK_TRANSFER_FAILED=       "transfer.failed";
    public static final String WEBHOOK_TRANSFER_REVERSED=       "transfer.reversed";
    public static final String FUND_CREDIT = "Your wallet has been credited with NGN %.2f. Transaction reference: %s.";
    public static final String OTP_RESEND_SUCCESSFUL = "Otp resend successful";
    public static final String LOGOUT_SUCCESSFUL = "Logout successful";
    public static final String KYC_DETAILS_SUBMITTED_SUCCESSFULLY = "Kyc details submitted successfully";
    public static final String KYC_DETAILS_RETRIEVED_SUCCESSFULLY = "Kyc details retrieved successfully";
    public static final String TRANSACTION_RETRIEVED_SUCCESSFULLY = "Transaction retrieved successful";
    public static final String TRANSACTIONS_RETRIEVED_SUCCESSFULLY ="Transactions retrieved successful";
    public static final String LOAN_APPLICATION_SUCCESSFUL = "Loan application successful";
    public static final String LOAN_APPROVED_SUCCESSFULLY = "Loan approved successfully";
    public static final String  LOAN_DISBURSED_SUCCESSFULLY="Loan disburse successfully";
    public static final String LOAN_REPAYMENT_PROCESSED="Loan repayment processed";
    public static final String LOAN_DETAILS_RETRIEVED_SUCCESSFULLY="Loan details retrieved successfully";
    public static final String LOANS_RETRIEVED_SUCCESSFULLY ="Loan retrieved successfully";
    public static final String BALANCE_RETRIEVED_SUCCESSFULLY="Wallet balance retrieved successfully";
    public static final String  FUNDING_INITIALIZED_REDIRECT_TO_COMPLETE_PAYMENT="Funding wallet initialized , Redirecting to complete payment";
    public static final String WALLET_FUNDED_SUCCESSFULLY="Wallet funded successfully";
    public static final String WALLET_CREATED_SUCCESSFULLY="Wallet created successfully";

}
