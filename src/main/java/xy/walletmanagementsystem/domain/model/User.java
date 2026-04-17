package xy.walletmanagementsystem.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import xy.walletmanagementsystem.domain.enums.UserRole;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private UserRole role;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;

}
//Full Name — String
//• Email Address — String (Unique)
//• Phone Number — String (Unique)
//• Password — Encrypted String
//• BVN/NIN (Mocked) — String
//• Account Status — Integer (Active, Suspended, Pending)
//• Created Date — Timestamp