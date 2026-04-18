package xy.walletmanagementsystem.infrastructure.output.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.domain.enums.OtpType;
import xy.walletmanagementsystem.domain.model.OtpDetails;
import xy.walletmanagementsystem.infrastructure.output.entities.OtpEntity;
import xy.walletmanagementsystem.infrastructure.output.mapper.OtpMapper;
import xy.walletmanagementsystem.infrastructure.output.repository.OtpRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpPersistenceAdapterTest {

    @Mock
    private OtpRepository otpRepository;
    @Mock
    private OtpMapper otpMapper;
    @InjectMocks
    private OtpPersistenceAdapter adapter;

    @Test
    void save_shouldPersistMappedEntity() {
        OtpDetails otp = OtpDetails.builder().email("john@example.com").build();
        OtpEntity entity = OtpEntity.builder().email("john@example.com").build();
        when(otpMapper.toEntity(otp)).thenReturn(entity);
        when(otpRepository.save(entity)).thenReturn(entity);
        when(otpMapper.toDomain(entity)).thenReturn(otp);

        adapter.save(otp);
        verify(otpRepository).save(entity);
    }

    @Test
    void findByEmailAndType_shouldReturnOptionalDomain() {
        OtpEntity entity = OtpEntity.builder().email("john@example.com").type(OtpType.PASSWORD_RESET).build();
        when(otpRepository.findByEmailAndType("john@example.com", OtpType.PASSWORD_RESET))
                .thenReturn(Optional.of(entity));
        when(otpMapper.toDomain(entity)).thenReturn(OtpDetails.builder().email("john@example.com").build());

        assertTrue(adapter.findByEmailAndType("john@example.com", OtpType.PASSWORD_RESET.name()).isPresent());
    }
}
