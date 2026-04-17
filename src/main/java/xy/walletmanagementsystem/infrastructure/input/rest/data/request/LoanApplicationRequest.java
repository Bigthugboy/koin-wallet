package xy.walletmanagementsystem.infrastructure.input.rest.data.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationRequest {
    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "0.01", message = "Loan amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationInDays;
}
