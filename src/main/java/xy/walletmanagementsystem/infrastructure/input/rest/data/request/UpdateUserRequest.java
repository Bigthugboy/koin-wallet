package xy.walletmanagementsystem.infrastructure.input.rest.data.request;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UpdateUserRequest {
    private String fullName;
    private String phoneNumber;
    private String email;

}
