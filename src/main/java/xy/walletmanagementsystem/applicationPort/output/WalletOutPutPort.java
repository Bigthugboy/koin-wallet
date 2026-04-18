package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.model.Wallet;

import java.util.Optional;

public interface WalletOutPutPort {
    Wallet save(Wallet wallet);
    Optional<Wallet> findByUserId(Long userId);
    Optional<Wallet> findById(Long walletId);
}
