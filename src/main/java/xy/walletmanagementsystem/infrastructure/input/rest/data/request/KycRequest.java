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
    private String bvn;
    private String nin;
}
