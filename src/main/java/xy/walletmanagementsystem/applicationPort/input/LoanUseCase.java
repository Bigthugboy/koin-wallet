package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Loan;

import java.math.BigDecimal;
import java.util.List;

public interface LoanUseCase {
    Loan applyForLoan(Long userId, BigDecimal amount, Integer durationInDays, String idempotencyKey) throws WalletManagementException;
    Loan approveLoan(Long loanId) throws WalletManagementException;
    Loan disburseLoan(Long loanId) throws WalletManagementException;
    void repayLoan(Long loanId, BigDecimal amount, String idempotencyKey) throws WalletManagementException;
    Loan getLoanDetails(Long loanId) throws WalletManagementException;
    List<Loan> getAllLoansForUser(Long userId) throws WalletManagementException;
    List<Loan> listAllLoans() throws WalletManagementException;
}
