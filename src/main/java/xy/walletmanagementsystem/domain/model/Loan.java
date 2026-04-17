package xy.walletmanagementsystem.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xy.walletmanagementsystem.domain.enums.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Loan {
    private String loanId;
    private String userId;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer durationInDays;
    private LoanStatus status;
    private String repaymentSchedule; // JSON representation
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
