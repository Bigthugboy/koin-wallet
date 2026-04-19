package xy.walletmanagementsystem.infrastructure.input.rest.data.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @jakarta.validation.constraints.Pattern(regexp = "^(\\+234|0)[789]\\d{9}$", message = "Invalid  phone number format")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;

    private boolean admin = false;
}
