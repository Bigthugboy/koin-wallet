package xy.walletmanagementsystem.infrastructure.input.rest.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String accessToken;
    @Builder.Default
    private String type = "Bearer";
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
}
