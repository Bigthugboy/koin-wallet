package xy.walletmanagementsystem.infrastructure.output.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.infrastructure.output.entities.KycEntity;

import java.util.Optional;

public interface KycRepository extends JpaRepository<KycEntity, String> {
    Optional<KycEntity> findByUserId(String userId);

    Optional<KycEntity> findByIdAndUserId(String id, String userId);
}
