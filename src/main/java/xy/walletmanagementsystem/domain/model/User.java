package xy.walletmanagementsystem.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private String id;
    private String name;
    private String email;
    private String password;
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