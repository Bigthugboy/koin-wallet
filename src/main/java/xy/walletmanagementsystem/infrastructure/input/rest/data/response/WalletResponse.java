package xy.walletmanagementsystem.infrastructure.input.rest.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xy.walletmanagementsystem.domain.enums.WalletStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponse {
    private Long walletId;
    private Long userId;
    private BigDecimal balance;
    private String currency;
    private WalletStatus status;
    private LocalDateTime dateCreated;
}
