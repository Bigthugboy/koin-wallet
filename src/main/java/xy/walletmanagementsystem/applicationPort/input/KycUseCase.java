package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Kyc;

public interface KycUseCase {
    Kyc submitKyc( Kyc kyc) throws WalletManagementException;
    Kyc getKycDetails(Long userId) throws WalletManagementException;
    Kyc updateVerificationStatus(Long kycId, String status) throws WalletManagementException;
}
