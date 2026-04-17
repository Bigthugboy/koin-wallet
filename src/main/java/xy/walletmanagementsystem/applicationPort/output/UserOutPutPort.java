package xy.walletmanagementsystem.applicationPort.output;

import aj.org.objectweb.asm.commons.Remapper;
import xy.walletmanagementsystem.domain.model.User;

import java.util.Optional;

public interface UserOutPutPort {
    
    Optional<User> findByEmail(String email);

    Optional<User> findById(String userId);
}
