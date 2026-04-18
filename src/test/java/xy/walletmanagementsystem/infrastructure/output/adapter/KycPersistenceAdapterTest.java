package xy.walletmanagementsystem.infrastructure.output.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.infrastructure.output.entities.KycEntity;
import xy.walletmanagementsystem.infrastructure.output.mapper.KycMapper;
import xy.walletmanagementsystem.infrastructure.output.repository.KycRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KycPersistenceAdapterTest {

    @Mock
    private KycRepository kycRepository;
    @Mock
    private KycMapper kycMapper;
    @InjectMocks
    private KycPersistenceAdapter adapter;

    @Test
    void save_shouldPersistMappedEntity() {
        Kyc kyc = Kyc.builder().id(1L).build();
        KycEntity entity = KycEntity.builder().id(1L).build();
        when(kycMapper.toEntity(kyc)).thenReturn(entity);
        when(kycRepository.save(entity)).thenReturn(entity);
        when(kycMapper.toDomain(entity)).thenReturn(kyc);

        adapter.save(kyc);
        verify(kycRepository).save(entity);
    }

    @Test
    void findByUserId_shouldReturnOptionalDomain() {
        KycEntity entity = KycEntity.builder().id(1L).userId(1L).build();
        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(entity));
        when(kycMapper.toDomain(entity)).thenReturn(Kyc.builder().id(1L).build());

        assertTrue(adapter.findByUserId(1L).isPresent());
    }

    @Test
    void findByIdAndUserId_shouldReturnDomain() {
        KycEntity entity = KycEntity.builder().id(1L).userId(1L).build();
        when(kycRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(entity));
        when(kycMapper.toDomain(entity)).thenReturn(Kyc.builder().id(1L).build());

        Kyc result = adapter.findByIdAndUserId(1L, 1L);
        assertEquals(1L, result.getId());
    }
}
