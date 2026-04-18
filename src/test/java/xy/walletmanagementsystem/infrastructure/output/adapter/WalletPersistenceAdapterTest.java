package xy.walletmanagementsystem.infrastructure.output.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.output.entities.WalletEntity;
import xy.walletmanagementsystem.infrastructure.output.mapper.WalletMapper;
import xy.walletmanagementsystem.infrastructure.output.repository.WalletRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletPersistenceAdapterTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletMapper walletMapper;
    @InjectMocks
    private WalletPersistenceAdapter adapter;

    @Test
    void save_shouldMapAndPersist() {
        Wallet wallet = Wallet.builder().walletId("w1").build();
        WalletEntity entity = WalletEntity.builder().id("w1").build();
        when(walletMapper.toEntity(wallet)).thenReturn(entity);
        when(walletRepository.save(entity)).thenReturn(entity);
        when(walletMapper.toDomain(entity)).thenReturn(wallet);

        adapter.save(wallet);
        verify(walletRepository).save(entity);
    }

    @Test
    void findByUserId_shouldReturnOptionalDomain() {
        WalletEntity entity = WalletEntity.builder().id("w1").userId("u1").build();
        when(walletRepository.findByUserId("u1")).thenReturn(Optional.of(entity));
        when(walletMapper.toDomain(entity)).thenReturn(Wallet.builder().walletId("w1").build());

        assertTrue(adapter.findByUserId("u1").isPresent());
    }
}
