package xy.walletmanagementsystem.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaystackInitializeRequest {
    private String email;
    private long amount; // in kobo
    private String reference;
    private String callback_url;
}
