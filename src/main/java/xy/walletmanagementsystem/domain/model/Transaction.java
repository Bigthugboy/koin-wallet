package xy.walletmanagementsystem.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xy.walletmanagementsystem.domain.enums.TransactionStatus;
import xy.walletmanagementsystem.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    private Long transactionId;
    private Long userId;
    private Long walletId;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private String referenceNumber;
    private LocalDateTime timestamp;
}
