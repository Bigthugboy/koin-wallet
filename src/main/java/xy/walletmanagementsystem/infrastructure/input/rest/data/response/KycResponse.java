package xy.walletmanagementsystem.infrastructure.input.rest.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xy.walletmanagementsystem.domain.enums.KycStatus;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KycResponse {
    private String id;
    private String userId;
    private String bvn;
    private String nin;
    private KycStatus status;
    private LocalDateTime createdDate;
}
