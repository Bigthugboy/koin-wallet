package xy.walletmanagementsystem.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.applicationPort.output.KycOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.enums.KycVerificationStatus;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.domain.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KycServiceTest {

    @Mock
    private KycOutPutPort kycOutPutPort;
    @Mock
    private UserOutPutPort userOutPutPort;

    @InjectMocks
    private KycService kycService;

    @Test
    void submitKyc_shouldRejectWhenBothBvnAndNinMissing() {
        assertThrows(WalletManagementException.class,
                () -> kycService.submitKyc(1L, "", ""));
    }

    @Test
    void submitKyc_shouldRejectInvalidBvnFormat() {
        assertThrows(WalletManagementException.class,
                () -> kycService.submitKyc(1L, "123", ""));
    }

    @Test
    void submitKyc_shouldSaveAndAutoVerifyWhenValid() throws Exception {
        User user = User.builder().id(1L).build();
        Kyc pending = Kyc.builder().id(1L).userId(1L).status(KycVerificationStatus.PENDING).build();
        when(userOutPutPort.findById(1L)).thenReturn(Optional.of(user));
        when(kycOutPutPort.findByUserId(1L)).thenReturn(Optional.empty());
        when(kycOutPutPort.save(any(Kyc.class))).thenAnswer(invocation -> {
            Kyc value = invocation.getArgument(0);
            if (value.getId() == null) {
                value.setId(1L);
            }
            return value;
        });
        when(kycOutPutPort.findByIdAndUserId(1L, 1L)).thenReturn(pending);

        Kyc result = kycService.submitKyc(1L, "12345678901", "");

        assertEquals(1L, result.getId());
    }

    @Test
    void getKycDetails_shouldFailWhenNotFound() {
        when(kycOutPutPort.findByUserId(1L)).thenReturn(Optional.empty());
        assertThrows(WalletManagementException.class, () -> kycService.getKycDetails(1L));
    }

    @Test
    void updateVerificationStatus_shouldUpdateStatus() throws Exception {
        Kyc kyc = Kyc.builder().id(1L).userId(1L).status(KycVerificationStatus.PENDING).build();
        when(kycOutPutPort.findById(1L)).thenReturn(Optional.of(kyc));
        when(kycOutPutPort.save(any(Kyc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Kyc updated = kycService.updateVerificationStatus(1L, "VERIFIED");
        assertEquals(KycVerificationStatus.VERIFIED, updated.getStatus());
    }
}
