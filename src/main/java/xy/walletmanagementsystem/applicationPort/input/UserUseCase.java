package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.User;

import java.util.Optional;

public interface UserUseCase {
        User createUser(User user);
        User updateUserProfile(String userId, User user) throws WalletManagementException;

        Optional<User> getUserDetails(String id) throws WalletManagementException;
}
