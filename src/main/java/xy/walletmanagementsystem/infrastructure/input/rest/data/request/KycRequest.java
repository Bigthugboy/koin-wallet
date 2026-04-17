package xy.walletmanagementsystem.infrastructure.input.rest.data.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KycRequest {
    @NotBlank(message = "BVN is required")
    private String bvn;

    @NotBlank(message = "NIN is required")
    private String nin;
}
