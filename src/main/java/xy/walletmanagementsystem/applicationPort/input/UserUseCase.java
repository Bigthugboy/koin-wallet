package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.model.User;

public interface UserUseCase {
        User createUser(User user);
        User updateUserProfile(String userId, User user);

}
