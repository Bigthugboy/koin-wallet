package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.output.PaymentProviderOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.TransactionOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.WalletOutPutPort;
import xy.walletmanagementsystem.domain.enums.AccountStatus;
import xy.walletmanagementsystem.domain.enums.TransactionStatus;
import xy.walletmanagementsystem.domain.enums.TransactionType;
import xy.walletmanagementsystem.domain.enums.WalletStatus;
import xy.walletmanagementsystem.domain.exception.IdempotencyException;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.PaystackFundingInitResponse;
import xy.walletmanagementsystem.domain.model.PaystackWebhookEvent;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

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
    private final PaymentProviderOutPutPort providerOutPutPort;



    @Override
    public Wallet createWallet(Long userId) throws WalletManagementException {
        if (userId == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        validateActiveUser(userId);
        if (walletOutPutPort.findByUserId(userId).isPresent()) {
            throw new WalletManagementException(ErrorMessages.USER_ALREADY_HAS_WALLET);
        }
        return walletOutPutPort.save(buildNewWallet(userId));
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


    @Override
    @Transactional
    public void fundWallet(Long userId, BigDecimal amount, String reference, String idempotencyKey)
            throws WalletManagementException {
        if (userId == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        validateActiveUser(userId);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }
        String keyToCheck = StringUtils.isNotBlank(idempotencyKey) ? idempotencyKey : reference;
        if (transactionOutPutPort.findByReference(keyToCheck).isPresent()) {
            throw new IdempotencyException(ErrorMessages.TRANSACTION_ALREADY_EXISTS);
        }
        Wallet savedWallet = creditWallet(userId, amount);
        transactionOutPutPort.save(buildCreditTransaction(userId, amount, keyToCheck, savedWallet,
                TransactionStatus.SUCCESSFUL, "direct-fund"));
    }

    @Override
    @Transactional
    public PaystackFundingInitResponse initializeFunding(User user, BigDecimal amount)
            throws WalletManagementException {
        if (user == null || user.getId() == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        validateActiveUser(user.getId());
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }

        Wallet wallet = walletOutPutPort.findByUserId(user.getId())
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.WALLET_NOT_FOUND));

        PaystackFundingInitResponse response = providerOutPutPort.initializeTransaction(user.getEmail(), amount);

        Transaction pending = buildCreditTransaction(
                user.getId(), amount, response.getReference(), wallet,
                TransactionStatus.PENDING, "paystack-init");
        transactionOutPutPort.save(pending);

        return response;
    }


    @Override
    @Transactional
    public void confirmFunding(PaystackWebhookEvent event) throws WalletManagementException {
        String reference = event.getReference();

        Transaction existing = findPendingTransaction(reference);
        if (existing == null) return;

        Wallet wallet = walletOutPutPort.findByUserId(existing.getUserId())
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.WALLET_NOT_FOUND));

        wallet.setBalance(wallet.getBalance().add(event.getAmount()));
        wallet.setDateUpdate(LocalDateTime.now());
        walletOutPutPort.save(wallet);

        existing.setStatus(TransactionStatus.SUCCESSFUL);
        existing.setDescription(event.getEvent());
        existing.setTimestamp(LocalDateTime.now());
        transactionOutPutPort.save(existing);


        log.info("Wallet funded for user {}. ref={} amount={}", existing.getUserId(), reference, event.getAmount());
    }

    @Override
    @Transactional
    public void markTransactionTerminal(String reference, TransactionStatus terminalStatus, String eventType)
            throws WalletManagementException {
        if (StringUtils.isBlank(reference)) {
            throw new WalletManagementException(ErrorMessages.REFERENCE_IS_REQUIRED);
        }

        Transaction existing = findPendingTransaction(reference);
        if (existing == null) return;

        existing.setStatus(terminalStatus);
        existing.setDescription(eventType);
        existing.setTimestamp(LocalDateTime.now());
        transactionOutPutPort.save(existing);

        log.info("Transaction {} marked {} via event '{}'.", reference, terminalStatus, eventType);
    }

    private Transaction findPendingTransaction(String reference) throws WalletManagementException {
        Transaction existing = transactionOutPutPort.findByReference(reference)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.TRANSACTION_REFERENCE_NOT_FOUND));

        if (existing.getStatus() != TransactionStatus.PENDING ) {
            log.warn("Transaction {} is already in terminal state {}. Skipping.", reference, existing.getStatus());
            return null;
        }
        return existing;
    }

    private void validateActiveUser(Long userId) throws WalletManagementException {
        User user = userOutPutPort.findById(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.USER_NOT_FOUND));
        if (AccountStatus.SUSPENDED.equals(user.getStatus())) {
            throw new WalletManagementException(ErrorMessages.ACCOUNT_SUSPENDED);
        }
    }

    private Wallet creditWallet(Long userId, BigDecimal amount) throws WalletManagementException {
        Wallet wallet = walletOutPutPort.findByUserId(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.WALLET_NOT_FOUND));
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setDateUpdate(LocalDateTime.now());
        return walletOutPutPort.save(wallet);
    }

    private static Transaction buildCreditTransaction(Long userId, BigDecimal amount,
                                                      String reference, Wallet wallet,
                                                      TransactionStatus status, String description) {
        return Transaction.builder()
                .userId(userId)
                .walletId(wallet.getWalletId())
                .type(TransactionType.CREDIT)
                .amount(amount)
                .status(status)
                .referenceNumber(reference)
                .description(description)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private static Wallet buildNewWallet(Long userId) {
        return Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency("NGN")
                .status(WalletStatus.ACTIVE)
                .dateCreated(LocalDateTime.now())
                .dateUpdate(LocalDateTime.now())
                .build();
    }
}
