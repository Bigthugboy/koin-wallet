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
        Kyc kyc = Kyc.builder().id("k1").build();
        KycEntity entity = KycEntity.builder().id("k1").build();
        when(kycMapper.toEntity(kyc)).thenReturn(entity);
        when(kycRepository.save(entity)).thenReturn(entity);
        when(kycMapper.toDomain(entity)).thenReturn(kyc);

        adapter.save(kyc);
        verify(kycRepository).save(entity);
    }

    @Test
    void findByUserId_shouldReturnOptionalDomain() {
        KycEntity entity = KycEntity.builder().id("k1").userId("u1").build();
        when(kycRepository.findByUserId("u1")).thenReturn(Optional.of(entity));
        when(kycMapper.toDomain(entity)).thenReturn(Kyc.builder().id("k1").build());

        assertTrue(adapter.findByUserId("u1").isPresent());
    }

    @Test
    void findByIdAndUserId_shouldReturnDomain() {
        KycEntity entity = KycEntity.builder().id("k1").userId("u1").build();
        when(kycRepository.findByIdAndUserId("k1", "u1")).thenReturn(Optional.of(entity));
        when(kycMapper.toDomain(entity)).thenReturn(Kyc.builder().id("k1").build());

        Kyc result = adapter.findByIdAndUserId("k1", "u1");
        assertEquals("k1", result.getId());
    }
}
