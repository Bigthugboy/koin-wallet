package xy.walletmanagementsystem.domain.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.input.KycUseCase;
import xy.walletmanagementsystem.applicationPort.output.KycOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.enums.KycVerificationStatus;
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
    public Kyc submitKyc(Long userId, String bvn, String nin) throws WalletManagementException {
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
    public Kyc getKycDetails(Long userId) throws WalletManagementException {
        return kycOutPutPort.findByUserId(userId)
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.KYC_NOT_FOUND));
    }

    @Override
    public Kyc updateVerificationStatus(Long kycId, String status) throws WalletManagementException {
        Kyc kyc = kycOutPutPort.findById(kycId).orElseThrow(() -> new WalletManagementException(ErrorMessages.KYC_NOT_FOUND));
        kyc.setStatus(KycVerificationStatus.valueOf(status));
        kyc.setDateUpdate(LocalDateTime.now());
        return kycOutPutPort.save(kyc);
    }

    public void verifyKyc(Long id, Long ownerId) throws WalletManagementException{
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
                approvedKyc.getStatus());
    }

    private void validateIdentifiers(Long id, Long userId) throws WalletManagementException {
        if (id == null) {
            throw new WalletManagementException(ErrorMessages.KYC_ID_REQUIRED);
        }

        if (userId == null) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
    }

    private Kyc getKycDetails(Long id, Long ownerId) throws WalletManagementException {
        Kyc kyc = kycOutPutPort.findByIdAndUserId(id, ownerId);
        if (kyc == null) {
            throw new WalletManagementException(ErrorMessages.KYC_DETAILS_NOT_FOUND);
        }
        return kyc;
    }

    private void validateKycEligibility(Kyc kyc) throws WalletManagementException {
        if (KycVerificationStatus.VERIFIED.equals(kyc.getStatus())) {
            throw new WalletManagementException(ErrorMessages.KYC_ALREADY_APPROVED);
        }
        if (KycVerificationStatus.REJECTED.equals(kyc.getStatus())) {
            throw new WalletManagementException(ErrorMessages.KYC_ALREADY_REJECTED);
        }
    }

    public Kyc approveKyc(Long id, Long ownerId, String reviewedBy)
            throws WalletManagementException {

        Kyc kyc = getKycDetails(id, ownerId);

        if (!KycVerificationStatus.PENDING.equals(kyc.getStatus())) {
            throw new WalletManagementException(ErrorMessages.KYC_STATUS_NOT_PENDING_REVIEW);
        }

        kyc.setStatus(KycVerificationStatus.VERIFIED);
        kyc.setDateUpdate(LocalDateTime.now(ZoneOffset.UTC));
        return kycOutPutPort.save(kyc);
    }

    public Kyc rejectKyc(Long id, Long ownerId, String reviewedBy, String reason)
            throws WalletManagementException {

        if (StringUtils.isBlank(reason)) {
            throw new WalletManagementException("Rejection reason is required");
        }

        Kyc kyc = getKycDetails(id, ownerId);

        if (!KycVerificationStatus.PENDING.equals(kyc.getStatus())) {
            throw new WalletManagementException(
                    "Only KYC records pending review can be rejected");
        }
        kyc.setStatus(KycVerificationStatus.REJECTED);
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

    private static Kyc buildKycDetails(Long userId, String bvn, String nin) {
        return Kyc.builder()
                .userId(userId)
                .bvn(bvn)
                .nin(nin)
                .status(KycVerificationStatus.PENDING)
                .dateCreated(LocalDateTime.now())
                .dateUpdate(LocalDateTime.now())
                .build();
    }
}
