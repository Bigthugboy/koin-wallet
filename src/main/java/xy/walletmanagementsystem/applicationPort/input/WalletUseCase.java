package xy.walletmanagementsystem.applicationPort.input;

import org.springframework.transaction.annotation.Transactional;
import xy.walletmanagementsystem.domain.enums.TransactionStatus;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.PaystackWebhookEvent;
import xy.walletmanagementsystem.domain.model.PaystackFundingInitResponse;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.domain.model.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface WalletUseCase {

    Wallet createWallet(Long userId) throws WalletManagementException;

    void fundWallet(Long userId, BigDecimal amount, String reference, String idempotencyKey) throws WalletManagementException;

    Wallet getWalletBalance(Long userId) throws WalletManagementException;

    List<Transaction> getTransactionHistory(Long userId) throws WalletManagementException;


    @Transactional
    PaystackFundingInitResponse initializeFunding(Long userId, BigDecimal amount) throws WalletManagementException;

    void confirmFunding(PaystackWebhookEvent event) throws WalletManagementException;

    void markTransactionTerminal(String reference, TransactionStatus terminalStatus, String eventType) throws WalletManagementException;
}
