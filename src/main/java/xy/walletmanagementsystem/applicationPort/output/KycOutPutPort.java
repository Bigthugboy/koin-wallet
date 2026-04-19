package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.model.Kyc;

import java.util.Optional;

public interface KycOutPutPort {
    Kyc save(Kyc kyc);
    Optional<Kyc> findByUserId(Long userId);
    Optional<Kyc> findById(Long kycId);

    Kyc findByIdAndUserId(Long id, Long userId);

    boolean isVerified(Long userId);
}
