package xy.walletmanagementsystem.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xy.walletmanagementsystem.domain.enums.OtpType;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpDetails {
    private String email;
    private String otp;
    private OtpType type;
    private LocalDateTime dateExpires;
    private LocalDateTime dateCreated;
}
