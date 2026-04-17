package xy.walletmanagementsystem.infrastructure.input.rest.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xy.walletmanagementsystem.domain.enums.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponse {
    private String loanId;
    private String userId;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer durationInDays;
    private LoanStatus status;
    private String repaymentSchedule;
    private LocalDateTime createdDate;
}
