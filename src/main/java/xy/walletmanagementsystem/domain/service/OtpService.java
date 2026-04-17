package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.input.OtpUseCase;
import xy.walletmanagementsystem.applicationPort.output.OtpOutPutPort;
import xy.walletmanagementsystem.domain.enums.OtpType;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.OtpDetails;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService implements OtpUseCase {

    private final OtpOutPutPort otpOutPutPort;

    @Override
    public OtpDetails generateOtp(String email, OtpType type) throws WalletManagementException {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        OtpDetails otpDetails = OtpDetails.builder()
                .email(email)
                .otp(otp)
                .type(type.name())
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .createdDate(LocalDateTime.now())
                .build();

        // Optional: Send via NotificationOutPutPort (Kafka)
        
        return otpOutPutPort.save(otpDetails);
    }

    @Override
    public boolean verifyOtp(String email, String otp, OtpType type) throws WalletManagementException {
        OtpDetails details = otpOutPutPort.findByEmailAndType(email, type.name())
                .orElseThrow(() -> new WalletManagementException("OTP not found or expired"));

        if (details.getExpiryDate().isBefore(LocalDateTime.now())) {
            otpOutPutPort.deleteByEmailAndType(email, type.name());
            throw new WalletManagementException("OTP has expired");
        }

        if (!details.getOtp().equals(otp)) {
            throw new WalletManagementException("Invalid OTP");
        }

        // Delete after successful verification
        otpOutPutPort.deleteByEmailAndType(email, type.name());
        return true;
    }

    @Override
    public OtpDetails resendOtp(String email, OtpType type) throws WalletManagementException {
        otpOutPutPort.deleteByEmailAndType(email, type.name());
        return generateOtp(email, type);
    }
}
