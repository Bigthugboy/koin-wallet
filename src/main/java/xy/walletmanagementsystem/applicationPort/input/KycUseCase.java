package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Kyc;

public interface KycUseCase {
    Kyc submitKyc(Long userId, String bvn, String nin) throws WalletManagementException;
    Kyc getKycDetails(Long userId) throws WalletManagementException;
    Kyc updateVerificationStatus(Long kycId, String status) throws WalletManagementException;
}
