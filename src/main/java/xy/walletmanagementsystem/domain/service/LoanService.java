package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xy.walletmanagementsystem.applicationPort.input.LoanUseCase;
import xy.walletmanagementsystem.applicationPort.output.KycOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.LoanOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.NotificationOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.TransactionOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.WalletOutPutPort;
import xy.walletmanagementsystem.domain.enums.KycVerificationStatus;
import xy.walletmanagementsystem.domain.enums.LoanStatus;
import xy.walletmanagementsystem.domain.enums.TransactionStatus;
import xy.walletmanagementsystem.domain.enums.TransactionType;
import xy.walletmanagementsystem.domain.exception.IdempotencyException;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.domain.model.Loan;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static xy.walletmanagementsystem.domain.messages.ConstantMessages.*;

@Service
@RequiredArgsConstructor
public class LoanService implements LoanUseCase {
    private final LoanOutPutPort loanOutPutPort;
    private final WalletOutPutPort walletOutPutPort;
    private final TransactionOutPutPort transactionOutPutPort;
    private final UserOutPutPort userOutPutPort;
    private final NotificationOutPutPort notificationOutPutPort;
    private final KycOutPutPort kycOutPutPort;

    @Override
    @Transactional
    public Loan applyForLoan(Long userId, BigDecimal amount, Integer durationInDays, String idempotencyKey) throws WalletManagementException {
        if (StringUtils.isNotBlank(idempotencyKey) && loanOutPutPort.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new IdempotencyException(ErrorMessages.TRANSACTION_ALREADY_EXISTS);
        }
        validateRequest(userId, amount, durationInDays);
        getKyc(userId);
        Wallet wallet = walletOutPutPort.findByUserId(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.WALLET_NOT_FOUND));
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.WALLET_NOT_FUNDED);
        }
        BigDecimal maxLoanAmount = wallet.getBalance().multiply(new BigDecimal("3"));
        if (amount.compareTo(maxLoanAmount) > 0) {
            throw new WalletManagementException(ErrorMessages.LOAN_AMOUNT_EXCEEDS_MAXIMUM_ALLOWED);
        }
        Loan loan = loanOutPutPort.save(buildLoanDetails(userId, amount, durationInDays, idempotencyKey));
        notifyLoanUser(userId, LOAN_APPLICATION);
        return loan;
    }

    private void getKyc(Long userId) throws WalletManagementException {
        Kyc kyc = kycOutPutPort.findByUserId(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.USER_KYC_NOT_FOUND));
        if (kyc.getStatus() != KycVerificationStatus.VERIFIED) {
            throw new WalletManagementException(ErrorMessages.KYC_NOT_VERIFIED);
        }
    }


    @Override
    public Loan approveLoan(Long loanId) throws WalletManagementException {
        validateLoanId(loanId);
        Loan loan = getLoan(loanId);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new WalletManagementException(ErrorMessages.LOAN_STATUS_IS_NOT_PENDING);
        }
        loan.setStatus(LoanStatus.APPROVED);
        loan.setDateUpdate(LocalDateTime.now());
        Loan savedLoan = loanOutPutPort.save(loan);
        notifyLoanUser(savedLoan.getUserId(), LOAN_APPROVED);
        return savedLoan;
    }

    private void validateLoanId(Long loanId) throws WalletManagementException {
        if (StringUtils.isBlank(String.valueOf(loanId))){
            throw new WalletManagementException(ErrorMessages.LOAN_ID_IS_REQUIRED);
        }
    }

    @Override
    @Transactional
    public Loan disburseLoan(Long loanId) throws WalletManagementException {
        validateLoanId(loanId);
        Loan loan = getApprovedLoan(loanId);
        Wallet wallet = getWallet(loan);
        updateWallet(wallet, loan);
        Loan savedLoan = updateLoan(loan);
        saveLoanTransaction(loan, wallet);
        notifyLoanUser(savedLoan.getUserId(), LOAN_DISBURSED);
        return savedLoan;
    }

    private @NonNull Wallet getWallet(Loan loan) throws WalletManagementException {
        return walletOutPutPort.findByUserId(loan.getUserId())
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.WALLET_NOT_FOUND));
    }


    private @NonNull Loan getLoan(Long loanId) throws WalletManagementException {
        return loanOutPutPort.findById(loanId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.LOAN_NOT_FOUND));
    }

    private @NonNull Loan getApprovedLoan(Long loanId) throws WalletManagementException {
        Loan loan = getLoan(loanId);
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new WalletManagementException(ErrorMessages.LOAN_STATUS_IS_NOT_APPROVED);
        }
        return loan;
    }


    @Override
    @Transactional
    public void repayLoan(Long loanId, BigDecimal amount, String idempotencyKey) throws WalletManagementException {
        validateLoanId(loanId);
        validateAmount(amount);
        validateKey(idempotencyKey);

        Loan loan = getLoan(loanId);
        if (loan.getStatus() != LoanStatus.DISBURSED && loan.getStatus() != LoanStatus.PARTIALLY_REPAID) {
            throw new WalletManagementException(ErrorMessages.LOAN_NOT_IN_PAYMENT_STATUS);
        }
        if (amount.compareTo(loan.getBalanceDue()) > 0) {
            throw new WalletManagementException(ErrorMessages.LOAN_PAYMENT_EXCEED_LOAN_BALANCE + loan.getBalanceDue());
        }

        Wallet wallet = getWallet(loan);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new WalletManagementException(ErrorMessages.INSUFFICIENT_FUNDS);
        }
        updateWalletForLoanRepayment(amount, wallet);
        updateLoanStatusForRepayment(loan, amount);
        saveLoanRepaymentTransaction(amount, loan, wallet, idempotencyKey);
        notifyLoanUser(loan.getUserId(), LOAN_REPAYMENT_SUCCESS + amount);

    }

    private void validateKey(String idempotencyKey) throws IdempotencyException {
        if (StringUtils.isNotBlank(idempotencyKey) && transactionOutPutPort.findByReference(idempotencyKey).isPresent()) {
            throw new IdempotencyException(ErrorMessages.TRANSACTION_ALREADY_EXISTS);
        }
    }

    private static void validateAmount(BigDecimal amount) throws WalletManagementException {
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.REPAYMENT_AMOUNT_MUST_BE_POSITIVE);
        }
    }

    @Override
    public Loan getLoanDetails(Long loanId) throws WalletManagementException {
        if(loanId == null) {
            throw new WalletManagementException(ErrorMessages.LOAN_ID_IS_REQUIRED);
        }
        return loanOutPutPort.findById(loanId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.LOAN_NOT_FOUND));
    }

    @Override
    public List<Loan> getAllLoansForUser(Long userId) throws WalletManagementException {
        if(userId == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        return loanOutPutPort.findByUserId(userId);
    }

    @Override
    public List<Loan> listAllLoans() throws WalletManagementException {
        return loanOutPutPort.findAll();
    }


    private static Loan buildLoanDetails(Long userId, BigDecimal amount, Integer durationInDays, String idempotencyKey) {
        BigDecimal interestRate = new BigDecimal("5.0");
        BigDecimal totalInterest = amount.multiply(interestRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmountDue = amount.add(totalInterest);
        return Loan.builder()
                .userId(userId)
                .amount(amount)
                .interestRate(interestRate)
                .durationInDays(durationInDays)
                .status(LoanStatus.PENDING)
                .repaymentSchedule(generateRepaymentSchedule(amount, interestRate, durationInDays))
                .balanceDue(totalAmountDue)
                .idempotencyKey(idempotencyKey)
                .dateCreated(LocalDateTime.now())
                .dateUpdate(LocalDateTime.now())
                .build();
    }

    private static String generateRepaymentSchedule(BigDecimal amount, BigDecimal interestRate, Integer durationInDays) {
        BigDecimal totalInterest = amount.multiply(interestRate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal totalAmountDue = amount.add(totalInterest);
        LocalDateTime dueDate = LocalDateTime.now().plusDays(durationInDays);
        // INSTALLMENT_TEMPLATE: installment(int), dueDate(String), amountDue(BigDecimal)
        return String.format(INSTALLMENT_TEMPLATE, 1, dueDate.toLocalDate().toString(), totalAmountDue);
    }

    private static void validateRequest(Long userId, BigDecimal amount, Integer durationInDays) throws WalletManagementException {
        if(userId == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.LOAN_AMOUNT_MUST_BE_POSITIVE);
        }
        if(durationInDays == null || durationInDays <= 0) {
            throw new WalletManagementException(ErrorMessages.LOAN_DURATION_MUST_BE_POSITIVE);
        }
    }
    private void updateLoanStatusForRepayment(Loan loan, BigDecimal amount) {
        loan.setBalanceDue(loan.getBalanceDue().subtract(amount));
        if (loan.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.REPAID);
        } else {
            loan.setStatus(LoanStatus.PARTIALLY_REPAID);
        }
        loan.setDateUpdate(LocalDateTime.now());
        loanOutPutPort.save(loan);
    }

    private void updateWalletForLoanRepayment(BigDecimal amount, Wallet wallet) {
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setDateUpdate(LocalDateTime.now());
        walletOutPutPort.save(wallet);
    }

    private void  saveLoanRepaymentTransaction(BigDecimal amount, Loan loan, Wallet wallet, String idempotencyKey) {
        String reference = StringUtils.isNotBlank(idempotencyKey) ? idempotencyKey : "REPAY-" + UUID.randomUUID().toString().substring(0, 8);
        Transaction transaction = Transaction.builder()
                .userId(loan.getUserId())
                .walletId(wallet.getWalletId())
                .type(TransactionType.REPAYMENT)
                .amount(amount)
                .status(TransactionStatus.SUCCESSFUL)
                .referenceNumber(reference)
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
        LocalDateTime now = LocalDateTime.now();
        loan.setStatus(LoanStatus.DISBURSED);
        loan.setDateDisbursed(now);
        loan.setDateUpdate(now);
        return loanOutPutPort.save(loan);
    }

    private void updateWallet(Wallet wallet, Loan loan) {
        wallet.setBalance(wallet.getBalance().add(loan.getAmount()));
        wallet.setDateUpdate(LocalDateTime.now());
        walletOutPutPort.save(wallet);
    }
@Async
protected void notifyLoanUser(Long userId, String message) {
        userOutPutPort.findById(userId)
                .ifPresent(user -> notificationOutPutPort.sendLoanNotification(user.getEmail(), message));
    }


}
