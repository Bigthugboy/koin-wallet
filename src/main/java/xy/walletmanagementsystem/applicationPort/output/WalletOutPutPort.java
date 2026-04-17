package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.model.Wallet;

import java.util.Optional;

public interface WalletOutPutPort {
    Wallet save(Wallet wallet);
    Optional<Wallet> findByUserId(String userId);
    Optional<Wallet> findById(String walletId);
}
