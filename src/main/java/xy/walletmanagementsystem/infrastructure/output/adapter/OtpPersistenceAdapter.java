package xy.walletmanagementsystem.infrastructure.output.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xy.walletmanagementsystem.applicationPort.output.OtpOutPutPort;
import xy.walletmanagementsystem.domain.model.OtpDetails;
import xy.walletmanagementsystem.infrastructure.output.entities.OtpEntity;
import xy.walletmanagementsystem.infrastructure.output.mapper.OtpMapper;
import xy.walletmanagementsystem.infrastructure.output.repository.OtpRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OtpPersistenceAdapter implements OtpOutPutPort {
    private final OtpRepository otpRepository;
    private final OtpMapper otpMapper;

    @Override
    public OtpDetails save(OtpDetails otpDetails) {
        OtpEntity entity = otpMapper.toEntity(otpDetails);
        if (entity.getId() == null) {
            entity.setId(java.util.UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        }
        OtpEntity savedEntity = otpRepository.save(entity);
        return otpMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<OtpDetails> findByEmailAndType(String email, String type) {
        return otpRepository.findByEmailAndType(email, xy.walletmanagementsystem.domain.enums.OtpType.valueOf(type))
                .map(otpMapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteByEmailAndType(String email, String type) {
        otpRepository.deleteByEmailAndType(email, xy.walletmanagementsystem.domain.enums.OtpType.valueOf(type));
    }
}
