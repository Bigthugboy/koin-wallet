package xy.walletmanagementsystem.infrastructure.output.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import xy.walletmanagementsystem.applicationPort.output.WalletOutPutPort;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.output.entities.WalletEntity;
import xy.walletmanagementsystem.infrastructure.output.mapper.WalletMapper;
import xy.walletmanagementsystem.infrastructure.output.repository.WalletRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WalletPersistenceAdapter implements WalletOutPutPort {
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Override
    public Wallet save(Wallet wallet) {
        WalletEntity entity = walletMapper.toEntity(wallet);
        WalletEntity savedEntity = walletRepository.save(entity);
        return walletMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Wallet> findByUserId(String userId) {
        return walletRepository.findByUserId(userId)
                .map(walletMapper::toDomain);
    }

    @Override
    public Optional<Wallet> findById(String walletId) {
        return walletRepository.findById(walletId)
                .map(walletMapper::toDomain);
    }
}
