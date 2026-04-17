package xy.walletmanagementsystem.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xy.walletmanagementsystem.domain.enums.AccountStatus;
import xy.walletmanagementsystem.domain.enums.UserRole;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private AccountStatus status;
    private UserRole role;
    private boolean emailVerified;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}