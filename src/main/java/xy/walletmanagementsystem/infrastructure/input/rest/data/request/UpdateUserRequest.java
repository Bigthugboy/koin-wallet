package xy.walletmanagementsystem.infrastructure.input.rest.data.request;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UpdateUserRequest {
    private String fullName;
    @jakarta.validation.constraints.Pattern(regexp = "^(\\+234|0)[789]\\d{9}$", message = "Invalid phone number format")
    private String phoneNumber;


}
