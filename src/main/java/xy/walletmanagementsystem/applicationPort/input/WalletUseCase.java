package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.output.paystack.PaystackFundingInitResponse;

import java.math.BigDecimal;
import java.util.List;

public interface WalletUseCase {
    Wallet createWallet(Long userId) throws WalletManagementException;
    void fundWallet(Long userId, BigDecimal amount, String reference, String idempotencyKey) throws WalletManagementException;
    Wallet getWalletBalance(Long userId) throws WalletManagementException;
    List<Transaction> getTransactionHistory(Long userId) throws WalletManagementException;
    PaystackFundingInitResponse initializeFunding(User user, BigDecimal amount) throws WalletManagementException;
    void confirmFunding(String reference, BigDecimal amount, String customerEmail) throws WalletManagementException;
}
