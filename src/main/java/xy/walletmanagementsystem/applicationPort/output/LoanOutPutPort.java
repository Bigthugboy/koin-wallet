package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.model.Loan;

import java.util.List;
import java.util.Optional;

public interface LoanOutPutPort {
    Loan save(Loan loan);
    Optional<Loan> findById(Long loanId);
    List<Loan> findByUserId(Long userId);
    List<Loan> findAll();
}
