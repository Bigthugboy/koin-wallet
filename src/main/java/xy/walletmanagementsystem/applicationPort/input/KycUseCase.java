package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Kyc;

public interface KycUseCase {
    Kyc submitKyc(String userId, String bvn, String nin) throws WalletManagementException;
    Kyc getKycDetails(String userId) throws WalletManagementException;
    Kyc updateVerificationStatus(String kycId, String status) throws WalletManagementException;
}
