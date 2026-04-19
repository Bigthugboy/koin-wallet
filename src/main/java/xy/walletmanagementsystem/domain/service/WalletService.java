package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.output.TransactionOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.WalletOutPutPort;
import xy.walletmanagementsystem.domain.enums.AccountStatus;
import xy.walletmanagementsystem.domain.enums.TransactionStatus;
import xy.walletmanagementsystem.domain.enums.TransactionType;
import xy.walletmanagementsystem.domain.enums.WalletStatus;
import xy.walletmanagementsystem.domain.exception.IdempotencyException;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;
import xy.walletmanagementsystem.infrastructure.output.paystack.PaystackFundingInitResponse;
import xy.walletmanagementsystem.infrastructure.output.paystack.PaystackService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static xy.walletmanagementsystem.domain.messages.EmailRegex.EMAIL_REGEX;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService implements WalletUseCase {

    private final WalletOutPutPort walletOutPutPort;
    private final TransactionOutPutPort transactionOutPutPort;
    private final UserOutPutPort userOutPutPort;
    private final PaystackService paystackService;

    @Override
    public Wallet createWallet(Long userId) throws WalletManagementException {
        if(userId == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        getUser(userId);
        if (walletOutPutPort.findByUserId(userId).isPresent()) {
            throw new WalletManagementException(ErrorMessages.USER_ALREADY_HAS_WALLET);
        }
        return walletOutPutPort.save(buildWalletDetails(userId));
    }

    private void getUser(Long userId) throws WalletManagementException {
        User user = userOutPutPort.findById(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.USER_NOT_FOUND));

        if (AccountStatus.SUSPENDED.equals(user.getStatus())) {
            throw new WalletManagementException(ErrorMessages.ACCOUNT_SUSPENDED);
        }

    }


    @Override
    @Transactional
    public void fundWallet(Long userId, BigDecimal amount, String reference, String idempotencyKey) throws WalletManagementException {
        if(userId == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        getUser(userId);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }
        String keyToCheck = StringUtils.isNotBlank(idempotencyKey) ? idempotencyKey : reference;
        if (transactionOutPutPort.findByReference(keyToCheck).isPresent()) {
            throw new IdempotencyException(ErrorMessages.TRANSACTION_ALREADY_EXISTS);
        }
        Wallet savedWallet = getWalletAndUpdateWallet(userId, amount);
        transactionOutPutPort.save(buildFundWalletTransaction(userId, amount, keyToCheck, savedWallet));
    }

    @Override
    public Wallet getWalletBalance(Long userId) throws WalletManagementException {
        if (userId == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        return walletOutPutPort.findByUserId(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.WALLET_NOT_FOUND));
    }

    @Override
    public List<Transaction> getTransactionHistory(Long userId) throws WalletManagementException {
        if (userId == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        return transactionOutPutPort.findByUserId(userId);
    }



    private Wallet getWalletAndUpdateWallet(Long userId, BigDecimal amount) throws WalletManagementException {
        Wallet wallet = walletOutPutPort.findByUserId(userId).orElseThrow(() -> new WalletManagementException(ErrorMessages.WALLET_NOT_FOUND));
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setDateUpdate(LocalDateTime.now());
        return walletOutPutPort.save(wallet);
    }

    private static Transaction buildFundWalletTransaction(Long userId, BigDecimal amount, String reference, Wallet wallet) {
        return Transaction.builder()
                .userId(userId)
                .walletId(wallet.getWalletId())
                .type(TransactionType.CREDIT)
                .amount(amount)
                .status(TransactionStatus.SUCCESSFUL)
                .referenceNumber(reference)
                .timestamp(LocalDateTime.now())
                .build();
    }
    private static Wallet buildWalletDetails(Long userId) {
        return Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency("NGN")
                .status(WalletStatus.ACTIVE)
                .dateCreated(LocalDateTime.now())
                .dateUpdate(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public PaystackFundingInitResponse initializeFunding(User user, BigDecimal amount) throws WalletManagementException {
        if (user == null || user.getId() == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        getUser(user.getId());
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }

        Wallet wallet = walletOutPutPort.findByUserId(user.getId())
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.WALLET_NOT_FOUND));

        // Call Paystack to initialize the transaction
        PaystackFundingInitResponse response = paystackService.initializeTransaction(user.getEmail(), amount);

        // Save a PENDING transaction locally for tracking
        Transaction pendingTx = Transaction.builder()
                .userId(user.getId())
                .walletId(wallet.getWalletId())
                .type(TransactionType.CREDIT)
                .amount(amount)
                .status(TransactionStatus.PENDING)
                .referenceNumber(response.getReference())
                .timestamp(LocalDateTime.now())
                .build();
        transactionOutPutPort.save(pendingTx);
        return response;
    }

    @Override
    @Transactional
    public void confirmFunding(String reference, BigDecimal amount, String customerEmail) throws WalletManagementException {
        validateFundingRequest(reference,amount,customerEmail);
        Transaction existing = checkIfTransactionIsAlreadyProcessed(reference);
        if (existing == null) return;

        // Credit the wallet
        Wallet wallet = walletOutPutPort.findByUserId(existing.getUserId())
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.WALLET_NOT_FOUND));
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setDateUpdate(LocalDateTime.now());
        walletOutPutPort.save(wallet);

        // Update the transaction to SUCCESSFUL
        existing.setStatus(TransactionStatus.SUCCESSFUL);
        existing.setTimestamp(LocalDateTime.now());
        transactionOutPutPort.save(existing);

        log.info("Wallet confirmed and funded for user {}", existing.getUserId());
    }

    private @Nullable Transaction checkIfTransactionIsAlreadyProcessed(String reference) throws WalletManagementException {
        Transaction existing = transactionOutPutPort.findByReference(reference)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.TRANSACTION_REFERENCE_NOT_FOUND));

        if (existing.getStatus() == TransactionStatus.SUCCESSFUL) {
            log.warn("Transaction {} already confirmed, skipping", reference);
            return null;
        }
        return existing;
    }

    private void validateFundingRequest(String reference, BigDecimal amount, String customerEmail) throws WalletManagementException {
        if (StringUtils.isBlank(reference)) {
            throw new WalletManagementException(ErrorMessages.REFERENCE_IS_REQUIRED);
        }
        if (amount == null) {
            throw new WalletManagementException(ErrorMessages.AMOUNT_IS_REQUIRED);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }
        if (StringUtils.isBlank(customerEmail)) {
            throw new WalletManagementException(ErrorMessages.EMAIL_IS_REQUIRED);
        }
        if (!customerEmail.matches(EMAIL_REGEX)) {
            throw new WalletManagementException(ErrorMessages.INVALID_EMAIL_FORMAT);
        }
    }




}



