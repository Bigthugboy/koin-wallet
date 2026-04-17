package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.model.Loan;

import java.util.List;
import java.util.Optional;

public interface LoanOutPutPort {
    Loan save(Loan loan);
    Optional<Loan> findById(String loanId);
    List<Loan> findByUserId(String userId);
    List<Loan> findAll();
}
