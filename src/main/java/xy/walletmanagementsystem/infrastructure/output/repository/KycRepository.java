package xy.walletmanagementsystem.infrastructure.output.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.infrastructure.output.entities.KycEntity;

import java.util.Optional;

public interface KycRepository extends JpaRepository<KycEntity, Long> {
    Optional<KycEntity> findByUserId(Long userId);

    Optional<KycEntity> findByIdAndUserId(Long id, Long userId);
}
