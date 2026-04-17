package xy.walletmanagementsystem.infrastructure.input.rest.data.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class WalletFundingRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum funding amount is 1.00")
    private BigDecimal amount;

    @NotBlank(message = "Payment reference is required")
    private String reference;
}
