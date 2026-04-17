package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.model.OtpDetails;

import java.util.Optional;

public interface OtpOutPutPort {
    OtpDetails save(OtpDetails otpDetails);
    Optional<OtpDetails> findByEmailAndType(String email, String type);
    void deleteByEmailAndType(String email, String type);
}
