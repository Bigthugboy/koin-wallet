package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.input.UserUseCase;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.model.User;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserOutPutPort userOutPutPort;

    @Override
    public User createUser(User user) {
        return userOutPutPort.save(user);
    }

    @Override
    public User updateUserProfile(String userId, User userDetails) {
        User existingUser = userOutPutPort.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
