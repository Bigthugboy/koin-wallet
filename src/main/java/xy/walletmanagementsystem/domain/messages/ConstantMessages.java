package xy.walletmanagementsystem.domain.messages;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstantMessages {
    public static final String LOAN_DISBURSED = "Your loan has been disbursed to your wallet.";
    public static final String LOAN_APPROVED = "Your loan has been approved.";
    public static final String LOAN_REPAYMENT_SUCCESS = "Loan repayment of %s NGN was successfully.";
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
    public static final String OTP_BODY = "Your OTP is %d. It expires in 10 minutes.";
    public static final String PROFILE_RETRIEVED =  "Profile retrieved successfully";
    public static final String PROFILE_UPDATED = "Profile updated successfully";

}
