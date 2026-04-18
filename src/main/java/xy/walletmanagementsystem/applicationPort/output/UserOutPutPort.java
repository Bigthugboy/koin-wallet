package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.model.User;

import java.util.Optional;

public interface UserOutPutPort {
    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long userId);

    boolean existsByEmail(String email);
}
