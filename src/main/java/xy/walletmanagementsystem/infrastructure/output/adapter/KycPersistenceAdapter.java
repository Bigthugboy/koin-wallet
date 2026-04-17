package xy.walletmanagementsystem.infrastructure.output.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import xy.walletmanagementsystem.applicationPort.output.KycOutPutPort;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;
import xy.walletmanagementsystem.infrastructure.output.entities.KycEntity;
import xy.walletmanagementsystem.infrastructure.output.mapper.KycMapper;
import xy.walletmanagementsystem.infrastructure.output.repository.KycRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KycPersistenceAdapter implements KycOutPutPort {
    private final KycRepository kycRepository;
    private final KycMapper kycMapper;

    @Override
    public Kyc save(Kyc kyc) {
        KycEntity entity = kycMapper.toEntity(kyc);
        KycEntity savedEntity = kycRepository.save(entity);
        return kycMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Kyc> findByUserId(String userId) {
        return kycRepository.findByUserId(userId)
                .map(kycMapper::toDomain);
    }

    @Override
    public Optional<Kyc> findById(String kycId) {
        return kycRepository.findById(kycId)
                .map(kycMapper::toDomain);
    }

    @Override
    public Kyc findByIdAndUserId(String id, String userId) {
        return kycRepository.findByIdAndUserId(id, userId)
                .map(kycMapper::toDomain)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.KYC_NOT_FOUND));
    }
}
