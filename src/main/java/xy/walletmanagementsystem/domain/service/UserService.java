package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.input.UserUseCase;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

import java.time.LocalDateTime;
import java.util.Optional;
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
    public User updateUserProfile(Long userId, User userDetails) throws WalletManagementException {
        User existingUser = userOutPutPort.findById(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.USER_NOT_FOUND));


        if (userDetails.getFullName() != null) {
            existingUser.setFullName(userDetails.getFullName());
        }
        if (userDetails.getPhoneNumber() != null && StringUtils.isNotBlank(userDetails.getPhoneNumber())) {
            existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        }
        existingUser.setDateUpdate(LocalDateTime.now());
        return userOutPutPort.save(existingUser);
    }

    @Override
    public Optional<User> getUserDetails(Long userId) throws WalletManagementException {
        if (userId == null){
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        return userOutPutPort.findById(userId);
    }
}
