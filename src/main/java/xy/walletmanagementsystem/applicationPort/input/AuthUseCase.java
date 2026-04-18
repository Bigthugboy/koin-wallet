package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.AuthResponse;
import xy.walletmanagementsystem.domain.model.User;

public interface AuthUseCase {
    User signup(User user, String password) throws WalletManagementException;

    AuthResponse login(String email, String password) throws WalletManagementException;

    void forgetPassword(String email) throws WalletManagementException;

    void resetPassword(String email, String otp, String newPassword) throws WalletManagementException;

    void logout(Long userId, String token) throws WalletManagementException;


}
