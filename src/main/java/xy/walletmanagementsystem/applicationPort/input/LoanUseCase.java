package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Loan;

import java.math.BigDecimal;
import java.util.List;

public interface LoanUseCase {
    Loan applyForLoan(String userId, BigDecimal amount, Integer durationInDays) throws WalletManagementException;
    Loan approveLoan(String loanId) throws WalletManagementException;
    Loan disburseLoan(String loanId) throws WalletManagementException;
    void repayLoan(String loanId, BigDecimal amount) throws WalletManagementException;
    Loan getLoanDetails(String loanId) throws WalletManagementException;
    List<Loan> getAllLoansForUser(String userId) throws WalletManagementException;
    List<Loan> listAllLoans() throws WalletManagementException;
}
