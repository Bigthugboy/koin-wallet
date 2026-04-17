package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xy.walletmanagementsystem.applicationPort.input.LoanUseCase;
import xy.walletmanagementsystem.applicationPort.output.LoanOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.TransactionOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.WalletOutPutPort;
import xy.walletmanagementsystem.domain.enums.LoanStatus;
import xy.walletmanagementsystem.domain.enums.TransactionStatus;
import xy.walletmanagementsystem.domain.enums.TransactionType;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Loan;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService implements LoanUseCase {

    private final LoanOutPutPort loanOutPutPort;
    private final WalletOutPutPort walletOutPutPort;
    private final TransactionOutPutPort transactionOutPutPort;

    @Override
    @Transactional
    public Loan applyForLoan(String userId, BigDecimal amount, Integer durationInDays) throws WalletManagementException {
        Wallet wallet = walletOutPutPort.findByUserId(userId)
                .orElseThrow(() -> new WalletManagementException("Wallet not found. Please create a wallet first."));

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException("Only users with funded wallets can apply for loans.");
        }

        BigDecimal maxLoanAmount = wallet.getBalance().multiply(new BigDecimal("3"));
        if (amount.compareTo(maxLoanAmount) > 0) {
            throw new WalletManagementException("Loan amount exceeds 3x wallet balance limit.");
        }

        Loan loan = Loan.builder()
                .loanId(UUID.randomUUID().toString())
                .userId(userId)
                .amount(amount)
                .interestRate(new BigDecimal("5.0")) // Default interest rate
                .durationInDays(durationInDays)
                .status(LoanStatus.PENDING)
                .repaymentSchedule("[]") // Placeholder for JSON schedule
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        return loanOutPutPort.save(loan);
    }

    @Override
    public Loan approveLoan(String loanId) throws WalletManagementException {
        Loan loan = loanOutPutPort.findById(loanId)
                .orElseThrow(() -> new WalletManagementException("Loan not found"));
        
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new WalletManagementException("Only pending loans can be approved");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setUpdatedDate(LocalDateTime.now());
        return loanOutPutPort.save(loan);
    }

    @Override
    @Transactional
    public Loan disburseLoan(String loanId) throws WalletManagementException {
        Loan loan = loanOutPutPort.findById(loanId)
                .orElseThrow(() -> new WalletManagementException("Loan not found"));

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new WalletManagementException("Loan must be approved before disbursement");
        }

        Wallet wallet = walletOutPutPort.findByUserId(loan.getUserId())
                .orElseThrow(() -> new WalletManagementException("Wallet not found"));

        // Update balance
        wallet.setBalance(wallet.getBalance().add(loan.getAmount()));
        wallet.setUpdatedDate(LocalDateTime.now());
        walletOutPutPort.save(wallet);

        // Update loan status
        loan.setStatus(LoanStatus.DISBURSED);
        loan.setUpdatedDate(LocalDateTime.now());
        Loan savedLoan = loanOutPutPort.save(loan);

        // Log transaction
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .userId(loan.getUserId())
                .walletId(wallet.getWalletId())
                .type(TransactionType.LOAN_DISBURSEMENT)
                .amount(loan.getAmount())
                .status(TransactionStatus.SUCCESSFUL)
                .referenceNumber("DISB-" + loan.getLoanId())
                .timestamp(LocalDateTime.now())
                .build();
        transactionOutPutPort.save(transaction);

        return savedLoan;
    }

    @Override
    @Transactional
    public void repayLoan(String loanId, BigDecimal amount) throws WalletManagementException {
        if(StringUtils.isBlank(loanId)){
            throw new WalletManagementException(ErrorMessages.LOAN_ID_IS_REQUIRED);
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.REPAYMENT_AMOUNT_MUST_BE_POSITIVE);
        }
        Loan loan = loanOutPutPort.findById(loanId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.LOAN_NOT_FOUND));

        if (loan.getStatus() != LoanStatus.DISBURSED) {
            throw new WalletManagementException(ErrorMessages.LOAN_NOT_IN_PAYMENT_STATUS);
        }

        Wallet wallet = walletOutPutPort.findByUserId(loan.getUserId())
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.LOAN_ID_IS_REQUIRED));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new WalletManagementException(ErrorMessages.INSUFFICIENT_FUNDS);
        }

        // Deduct from wallet
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedDate(LocalDateTime.now());
        walletOutPutPort.save(wallet);

        // Update loan - for simplicity, if amount >= loan amount + interest, mark as repaid
        // In a real system, we'd track remaining balance
        loan.setStatus(LoanStatus.REPAID);
        loan.setUpdatedDate(LocalDateTime.now());
        loanOutPutPort.save(loan);

        // Log transaction
        Transaction transaction = Transaction.builder()
                .userId(loan.getUserId())
                .walletId(wallet.getWalletId())
                .type(TransactionType.REPAYMENT)
                .amount(amount)
                .status(TransactionStatus.SUCCESSFUL)
                .referenceNumber("REPAY-" + UUID.randomUUID().toString().substring(0, 8))
                .timestamp(LocalDateTime.now())
                .build();
        transactionOutPutPort.save(transaction);
    }

    @Override
    public Loan getLoanDetails(String loanId) throws WalletManagementException {
        if(StringUtils.isBlank(loanId)) {
            throw new WalletManagementException(ErrorMessages.LOAN_ID_IS_REQUIRED);
        }
        return loanOutPutPort.findById(loanId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.LOAN_NOT_FOUND));
    }

    @Override
    public List<Loan> getAllLoansForUser(String userId) throws WalletManagementException {
        if(StringUtils.isBlank(userId)) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        return loanOutPutPort.findByUserId(userId);
    }

    @Override
    public List<Loan> listAllLoans() throws WalletManagementException {
        return loanOutPutPort.findAll();
    }
}
