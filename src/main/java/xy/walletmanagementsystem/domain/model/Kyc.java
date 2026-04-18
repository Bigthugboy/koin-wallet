package xy.walletmanagementsystem.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xy.walletmanagementsystem.domain.enums.KycStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Kyc {
    private String id;
    private String userId;
    private String bvn;
    private String nin;
    private KycStatus kycStatus;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
