package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.enums.OtpType;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.OtpDetails;

public interface OtpUseCase {
    OtpDetails generateOtp(String email, OtpType type) throws WalletManagementException;
    boolean verifyOtp(String email, String otp, OtpType type) throws WalletManagementException;
    void resendOtp(String email, OtpType type) throws WalletManagementException;



}
