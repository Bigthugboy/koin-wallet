package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.model.Kyc;

import java.util.Optional;

public interface KycOutPutPort {
    Kyc save(Kyc kyc);
    Optional<Kyc> findByUserId(String userId);
    Optional<Kyc> findById(String kycId);

    Kyc findByIdAndUserId(String id, String userId);
}
