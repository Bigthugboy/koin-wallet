package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.input.KycUseCase;
import xy.walletmanagementsystem.applicationPort.output.KycOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.enums.KycVerificationStatus;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycService implements KycUseCase {

    private final KycOutPutPort kycOutPutPort;
    private final UserOutPutPort userOutPutPort;

    @Override
    public Kyc submitKyc(String userId, String bvn, String nin) throws WalletManagementException {
        if (userOutPutPort.findById(userId).isEmpty()) {
            throw new WalletManagementException(ErrorMessages.USER_NOT_FOUND);
        }
        if (kycOutPutPort.findByUserId(userId).isPresent()) {
            throw new WalletManagementException(ErrorMessages.KYC_ALREADY_SUBMITTED);
        }
        Kyc kyc = Kyc.builder()
                .userId(userId)
                .bvn(bvn)
                .nin(nin)
                .status(KycVerificationStatus.PENDING)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
        return kycOutPutPort.save(kyc);
    }

    @Override
    public Kyc getKycDetails(String userId) throws WalletManagementException {
        return kycOutPutPort.findByUserId(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.KYC_NOT_FOUND));
    }

    @Override
    public Kyc updateVerificationStatus(String kycId, String status) throws WalletManagementException {
        Kyc kyc = kycOutPutPort.findById(kycId).orElseThrow(() -> new WalletManagementException(ErrorMessages.KYC_NOT_FOUND));
        kyc.setStatus(KycVerificationStatus.valueOf(status));
        kyc.setUpdatedDate(LocalDateTime.now());
        return kycOutPutPort.save(kyc);
    }
}
