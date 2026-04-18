package xy.walletmanagementsystem.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.applicationPort.output.KycOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.enums.KycStatus;
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
                () -> kycService.submitKyc("user-1", "", ""));
    }

    @Test
    void submitKyc_shouldRejectInvalidBvnFormat() {
        assertThrows(WalletManagementException.class,
                () -> kycService.submitKyc("user-1", "123", ""));
    }

    @Test
    void submitKyc_shouldSaveAndAutoVerifyWhenValid() throws Exception {
        User user = User.builder().id("user-1").build();
        Kyc pending = Kyc.builder().id("kyc-1").userId("user-1").kycStatus(KycStatus.PENDING).build();
        when(userOutPutPort.findById("user-1")).thenReturn(Optional.of(user));
        when(kycOutPutPort.findByUserId("user-1")).thenReturn(Optional.empty());
        when(kycOutPutPort.save(any(Kyc.class))).thenAnswer(invocation -> {
            Kyc value = invocation.getArgument(0);
            if (value.getId() == null) {
                value.setId("kyc-1");
            }
            return value;
        });
        when(kycOutPutPort.findByIdAndUserId("kyc-1", "user-1")).thenReturn(pending);

        Kyc result = kycService.submitKyc("user-1", "12345678901", "");

        assertEquals("kyc-1", result.getId());
    }

    @Test
    void getKycDetails_shouldFailWhenNotFound() {
        when(kycOutPutPort.findByUserId("user-1")).thenReturn(Optional.empty());
        assertThrows(WalletManagementException.class, () -> kycService.getKycDetails("user-1"));
    }

    @Test
    void updateVerificationStatus_shouldUpdateStatus() throws Exception {
        Kyc kyc = Kyc.builder().id("kyc-1").userId("user-1").kycStatus(KycStatus.PENDING).build();
        when(kycOutPutPort.findById("kyc-1")).thenReturn(Optional.of(kyc));
        when(kycOutPutPort.save(any(Kyc.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Kyc updated = kycService.updateVerificationStatus("kyc-1", "VERIFIED");
        assertEquals(KycStatus.VERIFIED, updated.getKycStatus());
    }
}
