package xy.walletmanagementsystem.domain.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.input.KycUseCase;
import xy.walletmanagementsystem.applicationPort.output.KycOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.enums.KycStatus;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycService implements KycUseCase {

    private final KycOutPutPort kycOutPutPort;
    private final UserOutPutPort userOutPutPort;

    @Override
    public Kyc submitKyc(String userId, String bvn, String nin) throws WalletManagementException {
        validateHasBvnOrNin(bvn, nin);
        if (userOutPutPort.findById(userId).isEmpty()) {
            throw new WalletManagementException(ErrorMessages.USER_NOT_FOUND);
        }
        if (kycOutPutPort.findByUserId(userId).isPresent()) {
            throw new WalletManagementException(ErrorMessages.KYC_ALREADY_SUBMITTED);
        }
        Kyc kyc = buildKycDetails(userId, bvn, nin);
        Kyc savedKyc = kycOutPutPort.save(kyc);
        verifyKyc(savedKyc.getId(), savedKyc.getUserId());
        return savedKyc;
    }

    @Override
    public Kyc getKycDetails(String userId) throws WalletManagementException {
        return kycOutPutPort.findByUserId(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.KYC_NOT_FOUND));
    }

    @Override
    public Kyc updateVerificationStatus(String kycId, String status) throws WalletManagementException {
        Kyc kyc = kycOutPutPort.findById(kycId).orElseThrow(() -> new WalletManagementException(ErrorMessages.KYC_NOT_FOUND));
        kyc.setKycStatus(KycStatus.valueOf(status));
        kyc.setDateUpdate(LocalDateTime.now());
        return kycOutPutPort.save(kyc);
    }


    public void verifyKyc(String id, String ownerId) throws WalletManagementException{
        validateIdentifiers(id, ownerId);
        Kyc kyc = getKycDetails(id, ownerId);
        log.info(
                "Starting manual KYC verification. kycId={}, userId={}",
                kyc.getId(),
                kyc.getUserId()
                );
        validateKycEligibility(kyc);
        Kyc approvedKyc = approveKyc(kyc.getId(), kyc.getUserId(), "system");

        log.info(
                "KYC submitted for manual review successfully. kycId={}, status={}",
                approvedKyc.getId(),
                approvedKyc.getKycStatus());
    }

    private void validateIdentifiers(String id, String userId) throws WalletManagementException {
        if (StringUtils.isBlank(id)) {
            throw new WalletManagementException(ErrorMessages.KYC_ID_REQUIRED);
        }

        if (StringUtils.isBlank(userId)) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
    }

    private Kyc getKycDetails(String id, String ownerId) throws WalletManagementException {
        Kyc kyc = kycOutPutPort.findByIdAndUserId(id.trim(), ownerId.trim());
        if (kyc == null) {
            throw new WalletManagementException(ErrorMessages.KYC_DETAILS_NOT_FOUND);
        }
        return kyc;
    }

    private void validateKycEligibility(Kyc kyc) throws WalletManagementException {
        if (KycStatus.VERIFIED.equals(kyc.getKycStatus())) {
            throw new WalletManagementException(ErrorMessages.KYC_ALREADY_APPROVED);
        }
        if (KycStatus.REJECTED.equals(kyc.getKycStatus())) {
            throw new WalletManagementException(ErrorMessages.KYC_ALREADY_REJECTED);
        }
    }

    public Kyc approveKyc(String id, String ownerId, String reviewedBy)
            throws WalletManagementException {

        Kyc kyc = getKycDetails(id, ownerId);

        if (!KycStatus.PENDING.equals(kyc.getKycStatus())) {
            throw new WalletManagementException(ErrorMessages.KYC_STATUS_NOT_PENDING_REVIEW);
        }

        kyc.setKycStatus(KycStatus.VERIFIED);
        kyc.setDateUpdate(LocalDateTime.now(ZoneOffset.UTC));
        return kycOutPutPort.save(kyc);
    }

    public Kyc rejectKyc(String id, String ownerId, String reviewedBy, String reason)
            throws WalletManagementException {

        if (StringUtils.isBlank(reason)) {
            throw new WalletManagementException("Rejection reason is required");
        }

        Kyc kyc = getKycDetails(id, ownerId);

        if (!KycStatus.PENDING.equals(kyc.getKycStatus())) {
            throw new WalletManagementException(
                    "Only KYC records pending review can be rejected");
        }
        kyc.setKycStatus(KycStatus.REJECTED);
        kyc.setDateUpdate(LocalDateTime.now(ZoneOffset.UTC));
        return kycOutPutPort.save(kyc);
    }

    private void validateHasBvnOrNin(String bvn, String nin) throws WalletManagementException {
        if (StringUtils.isBlank(bvn) && StringUtils.isBlank(nin)) {
            throw new WalletManagementException(ErrorMessages.BVN_OR_NIN_MUST_IS_REQUIRED_FOR_USER_KYC_VERIFICATION);
        }
        if (StringUtils.isNotBlank(bvn) && !bvn.matches("^\\d{11}$")) {
            throw new WalletManagementException(ErrorMessages.BVN_MUST_CONTAIN_EXACTLY_11_DIGITS);
        }
        if(StringUtils.isNotBlank(nin) && !nin.matches("^\\d{11}$")) {
            throw new WalletManagementException(ErrorMessages.NIN_MUST_CONTAIN_EXACTLY_11_DIGITS);
        }

    }

    private static Kyc buildKycDetails(String userId, String bvn, String nin) {
        return Kyc.builder()
                .userId(userId)
                .bvn(bvn)
                .nin(nin)
                .kycStatus(KycStatus.PENDING)
                .dateCreated(LocalDateTime.now())
                .dateUpdate(LocalDateTime.now())
                .build();
    }
}
