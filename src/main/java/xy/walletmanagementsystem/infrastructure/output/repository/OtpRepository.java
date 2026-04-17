package xy.walletmanagementsystem.infrastructure.output.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xy.walletmanagementsystem.domain.enums.OtpType;
import xy.walletmanagementsystem.infrastructure.output.entities.OtpEntity;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {
    Optional<OtpEntity> findByEmailAndType(String email, OtpType type);
    void deleteByEmailAndType(String email, OtpType type);
}
