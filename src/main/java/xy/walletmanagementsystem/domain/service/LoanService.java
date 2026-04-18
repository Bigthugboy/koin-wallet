package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xy.walletmanagementsystem.applicationPort.input.LoanUseCase;
import xy.walletmanagementsystem.applicationPort.output.LoanOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.NotificationOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.TransactionOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
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
    private final UserOutPutPort userOutPutPort;
    private final NotificationOutPutPort notificationOutPutPort;

    @Override
    @Transactional
    public Loan applyForLoan(String userId, BigDecimal amount, Integer durationInDays) throws WalletManagementException {
        validateRequest(userId, amount, durationInDays);
        Wallet wallet = walletOutPutPort.findByUserId(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.WALLET_NOT_FOUND));
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.WALLET_NOT_FUNDED);
        }
        BigDecimal maxLoanAmount = wallet.getBalance().multiply(new BigDecimal("3"));
        if (amount.compareTo(maxLoanAmount) > 0) {
            throw new WalletManagementException(ErrorMessages.LOAN_AMOUNT_EXCEEDS_MAXIMUM_ALLOWED);
        }
        Loan loan = loanOutPutPort.save(buildLoanDetails(userId, amount, durationInDays));
        notifyLoanUser(userId, "Your loan application has been submitted and is pending approval.");
        return loan;
    }


    @Override
    public Loan approveLoan(String loanId) throws WalletManagementException {
        Loan loan = loanOutPutPort.findById(loanId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.LOAN_NOT_FOUND));
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new WalletManagementException(ErrorMessages.LOAN_STATUS_IS_NOT_PENDING);
        }
        loan.setStatus(LoanStatus.APPROVED);
        loan.setUpdatedDate(LocalDateTime.now());
        Loan savedLoan = loanOutPutPort.save(loan);
        notifyLoanUser(savedLoan.getUserId(), "Your loan has been approved.");
        return savedLoan;
    }

    @Override
    @Transactional
    public Loan disburseLoan(String loanId) throws WalletManagementException {
        if(StringUtils.isBlank(loanId)) {
            throw new WalletManagementException(ErrorMessages.LOAN_ID_IS_REQUIRED);
        }
        Loan loan = loanOutPutPort.findById(loanId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.LOAN_NOT_FOUND));

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new WalletManagementException(ErrorMessages.LOAN_STATUS_IS_NOT_APPROVED);
        }

        Wallet wallet = walletOutPutPort.findByUserId(loan.getUserId())
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.WALLET_NOT_FOUND));

        // Update balance
        updateWallet(wallet, loan);

        // Update loan status
        Loan savedLoan = updateLoan(loan);

        // Log transaction
        saveLoanTransaction(loan, wallet);
        notifyLoanUser(savedLoan.getUserId(), "Your loan has been disbursed to your wallet.");
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
        updateWalletForLoanRepayment(amount, wallet);

        // Update loan - for simplicity, if amount >= loan amount + interest, mark as repaid
        // In a real system, we'd track remaining balance
        updateLoanStatusForRepayment(loan);

        // Log transaction
         saveLoanRepaymentTransaction(amount, loan, wallet);
        notifyLoanUser(loan.getUserId(), "Loan repayment of " + amount + " was successful.");

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


    private static Loan buildLoanDetails(String userId, BigDecimal amount, Integer durationInDays) {
        return Loan.builder()
                .userId(userId)
                .amount(amount)
                .interestRate(new BigDecimal("5.0")) // Default interest rate
                .durationInDays(durationInDays)
                .status(LoanStatus.PENDING)
                .repaymentSchedule("[]") // Placeholder for JSON schedule
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
    }

    private static void validateRequest(String userId, BigDecimal amount, Integer durationInDays) throws WalletManagementException {
        if(StringUtils.isBlank(userId)) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.LOAN_AMOUNT_MUST_BE_POSITIVE);
        }
        if(durationInDays == null || durationInDays <= 0) {
            throw new WalletManagementException(ErrorMessages.LOAN_DURATION_MUST_BE_POSITIVE);
        }
    }
    private void updateLoanStatusForRepayment(Loan loan) {
        loan.setStatus(LoanStatus.REPAID);
        loan.setUpdatedDate(LocalDateTime.now());
        loanOutPutPort.save(loan);
    }

    private void updateWalletForLoanRepayment(BigDecimal amount, Wallet wallet) {
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedDate(LocalDateTime.now());
        walletOutPutPort.save(wallet);
    }

    private void  saveLoanRepaymentTransaction(BigDecimal amount, Loan loan, Wallet wallet) {
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

    private void saveLoanTransaction(Loan loan, Wallet wallet) {
        Transaction transaction = Transaction.builder()
                .userId(loan.getUserId())
                .walletId(wallet.getWalletId())
                .type(TransactionType.LOAN_DISBURSEMENT)
                .amount(loan.getAmount())
                .status(TransactionStatus.SUCCESSFUL)
                .referenceNumber("DISB-" + loan.getLoanId())
                .timestamp(LocalDateTime.now())
                .build();
        transactionOutPutPort.save(transaction);
    }

    private Loan updateLoan(Loan loan) {
        loan.setStatus(LoanStatus.DISBURSED);
        loan.setUpdatedDate(LocalDateTime.now());
        return loanOutPutPort.save(loan);
    }

    private void updateWallet(Wallet wallet, Loan loan) {
        wallet.setBalance(wallet.getBalance().add(loan.getAmount()));
        wallet.setUpdatedDate(LocalDateTime.now());
        walletOutPutPort.save(wallet);
    }

    private void notifyLoanUser(String userId, String message) {
        userOutPutPort.findById(userId)
                .ifPresent(user -> notificationOutPutPort.sendLoanNotification(user.getEmail(), message));
    }


}
