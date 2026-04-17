package xy.walletmanagementsystem.infrastructure.output.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xy.walletmanagementsystem.infrastructure.output.entities.TransactionEntity;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
    List<TransactionEntity> findByUserId(String userId);
    Optional<TransactionEntity> findByReferenceNumber(String referenceNumber);
}
