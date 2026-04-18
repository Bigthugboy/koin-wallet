package xy.walletmanagementsystem.infrastructure.output.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xy.walletmanagementsystem.infrastructure.output.entities.LoanEntity;

import java.util.List;

public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
    List<LoanEntity> findByUserId(String userId);
}
