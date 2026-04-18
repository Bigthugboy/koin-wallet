package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.output.TransactionOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.WalletOutPutPort;
import xy.walletmanagementsystem.domain.enums.TransactionStatus;
import xy.walletmanagementsystem.domain.enums.TransactionType;
import xy.walletmanagementsystem.domain.enums.WalletStatus;
import xy.walletmanagementsystem.domain.exception.IdempotencyException;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService implements WalletUseCase {

    private final WalletOutPutPort walletOutPutPort;
    private final TransactionOutPutPort transactionOutPutPort;

    @Override
    public Wallet createWallet(Long userId) throws WalletManagementException {
        if(userId == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        if (walletOutPutPort.findByUserId(userId).isPresent()) {
            throw new WalletManagementException(ErrorMessages.USER_ALREADY_HAS_WALLET);
        }
        return walletOutPutPort.save(buildWalletDetails(userId));
    }


    @Override
    @Transactional
    public void fundWallet(Long userId, BigDecimal amount, String reference) throws WalletManagementException {
        if(userId == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }
        if (transactionOutPutPort.findByReference(reference).isPresent()) {
            throw new IdempotencyException("Transaction with reference " + reference + " already processed");
        }
        Wallet savedWallet = getWalletAndUpdateWallet(userId, amount);
        transactionOutPutPort.save(buildFundWalletTransaction(userId, amount, reference, savedWallet));
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



}
