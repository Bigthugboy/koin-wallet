package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.AuthResponse;

public interface AuthUseCase {
    AuthResponse login(String email, String password) throws WalletManagementException;

    void forgetPassword(String email) throws WalletManagementException;

    void resetPassword(String email, String otp, String newPassword) throws WalletManagementException;

    void logout(String userId, String token) throws WalletManagementException;

    void changePassword(String userId, String currentPassword, String newPassword, String confirmNewPassword) throws WalletManagementException;
}
