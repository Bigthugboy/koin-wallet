package xy.walletmanagementsystem.domain.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.input.OtpUseCase;
import xy.walletmanagementsystem.applicationPort.output.OtpOutPutPort;
import xy.walletmanagementsystem.domain.enums.OtpType;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.OtpDetails;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService implements OtpUseCase {

    private final OtpOutPutPort otpOutPutPort;

    @Override
    public OtpDetails generateOtp(String email, OtpType type) throws WalletManagementException {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        OtpDetails otpDetails = buildOtpDetails(email, type, otp);
        // Optional: Send via NotificationOutPutPort (Kafka)
        return otpOutPutPort.save(otpDetails);
    }

    private static OtpDetails buildOtpDetails(String email, OtpType type, String otp) {
        return OtpDetails.builder()
                .email(email)
                .otp(otp)
                .type(type)
                .dateExpires(LocalDateTime.now().plusMinutes(10))
                .dateCreated(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean verifyOtp(String email, String otp, OtpType type) throws WalletManagementException {
        OtpDetails details = otpOutPutPort.findByEmailAndType(email, type.name())
                .orElseThrow(() -> new WalletManagementException(ErrorMessages.OTP_NOT_FOUND));
        if (details.getDateExpires().isBefore(LocalDateTime.now())) {
            otpOutPutPort.deleteByEmailAndType(email, type.name());
            throw new WalletManagementException(ErrorMessages.OTP_EXPIRED);
        }
        if (!details.getOtp().equals(otp)) {
            throw new WalletManagementException(ErrorMessages.INVALID_OTP);
        }
        otpOutPutPort.deleteByEmailAndType(email, type.name());
        return true;
    }

    @Override
    public OtpDetails resendOtp(String email, OtpType type) throws WalletManagementException {
        validateRequests(email, type);
        otpOutPutPort.deleteByEmailAndType(email, type.name());
        return generateOtp(email, type);
    }

    private void validateRequests(String email, OtpType type) throws WalletManagementException {
        if(StringUtils.isBlank(email) || type == null) {
            throw new WalletManagementException(ErrorMessages.EMAIL_AND_TYPE_REQUIRED);
        }
        if (StringUtils.isBlank(email)) {
            throw new WalletManagementException(ErrorMessages.EMAIL_IS_REQUIRED);
        }
         if (type == null) {
            throw new WalletManagementException(ErrorMessages.OTP_TYPE_IS_REQUIRED);
        }
    }

    @Override
    public boolean isVerified(String email) throws WalletManagementException {

        return false;
    }
}
