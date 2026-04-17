package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface WalletUseCase {
    Wallet createWallet(String userId) throws WalletManagementException;
    void fundWallet(String userId, BigDecimal amount, String reference) throws WalletManagementException;
    Wallet getWalletBalance(String userId) throws WalletManagementException;
    List<Transaction> getTransactionHistory(String userId) throws WalletManagementException;
}
