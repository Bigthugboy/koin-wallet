package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.input.UserUseCase;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

import java.time.LocalDateTime;
import java.util.logging.ErrorManager;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserOutPutPort userOutPutPort;

    @Override
    public User createUser(User user) {
        return userOutPutPort.save(user);
    }

    @Override
    public User updateUserProfile(String userId, User userDetails) throws WalletManagementException {
        User existingUser = userOutPutPort.findById(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.USER_NOT_FOUND));

        if (userDetails.getFullName() != null) {
            existingUser.setFullName(userDetails.getFullName());
        }
        if (userDetails.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        }
        existingUser.setUpdatedDate(LocalDateTime.now());
        return userOutPutPort.save(existingUser);
    }
}
