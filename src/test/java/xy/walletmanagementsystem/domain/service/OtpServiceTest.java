package xy.walletmanagementsystem.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.applicationPort.output.EmailOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.OtpOutPutPort;
import xy.walletmanagementsystem.domain.enums.OtpType;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.OtpDetails;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpOutPutPort otpOutPutPort;
    @Mock
    private EmailOutPutPort emailOutPutPort;

    @InjectMocks
    private OtpService otpService;

    @Test
    void generateOtp_shouldPersistOtpWithExpiry() throws Exception {
        when(otpOutPutPort.save(any(OtpDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OtpDetails result = otpService.generateOtp("john@example.com", OtpType.FORGOT_PASSWORD);

        assertNotNull(result.getOtp());
        assertEquals(6, result.getOtp().length());
        assertTrue(result.getExpiryDate().isAfter(LocalDateTime.now()));
        verify(emailOutPutPort).sendEmail(any());
    }

    @Test
    void verifyOtp_shouldFailWhenExpired() {
        OtpDetails details = OtpDetails.builder()
                .email("john@example.com")
                .otp("123456")
                .type(OtpType.FORGOT_PASSWORD)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build();
        when(otpOutPutPort.findByEmailAndType("john@example.com", OtpType.FORGOT_PASSWORD.name()))
                .thenReturn(Optional.of(details));

        assertThrows(WalletManagementException.class,
                () -> otpService.verifyOtp("john@example.com", "123456", OtpType.FORGOT_PASSWORD));
        verify(otpOutPutPort).deleteByEmailAndType("john@example.com", OtpType.FORGOT_PASSWORD.name());
    }

    @Test
    void verifyOtp_shouldFailWhenOtpDoesNotMatch() {
        OtpDetails details = OtpDetails.builder()
                .email("john@example.com")
                .otp("111111")
                .type(OtpType.FORGOT_PASSWORD)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();
        when(otpOutPutPort.findByEmailAndType("john@example.com", OtpType.FORGOT_PASSWORD.name()))
                .thenReturn(Optional.of(details));

        assertThrows(WalletManagementException.class,
                () -> otpService.verifyOtp("john@example.com", "123456", OtpType.FORGOT_PASSWORD));
    }

    @Test
    void verifyOtp_shouldPassAndDeleteOtpWhenValid() throws Exception {
        OtpDetails details = OtpDetails.builder()
                .email("john@example.com")
                .otp("123456")
                .type(OtpType.FORGOT_PASSWORD)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();
        when(otpOutPutPort.findByEmailAndType("john@example.com", OtpType.FORGOT_PASSWORD.name()))
                .thenReturn(Optional.of(details));

        boolean result = otpService.verifyOtp("john@example.com", "123456", OtpType.FORGOT_PASSWORD);

        assertTrue(result);
        verify(otpOutPutPort).deleteByEmailAndType("john@example.com", OtpType.FORGOT_PASSWORD.name());
    }

    @Test
    void resendOtp_shouldDeleteOldOtpThenGenerateNew() throws Exception {
        when(otpOutPutPort.save(any(OtpDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        otpService.resendOtp("john@example.com", OtpType.FORGOT_PASSWORD);

        verify(otpOutPutPort).deleteByEmailAndType("john@example.com", OtpType.FORGOT_PASSWORD.name());
        verify(otpOutPutPort).save(any(OtpDetails.class));
        verify(emailOutPutPort).sendEmail(any());
    }

    @Test
    void resendOtp_shouldFailValidationWhenEmailMissing() {
        assertThrows(WalletManagementException.class,
                () -> otpService.resendOtp("", OtpType.FORGOT_PASSWORD));
    }
}
