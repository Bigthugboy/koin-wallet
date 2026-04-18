package xy.walletmanagementsystem.infrastructure.output.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xy.walletmanagementsystem.domain.enums.KycStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(unique = true)
    private String bvn;

    @Column(unique = true)
    private String nin;

    @Enumerated(EnumType.STRING)
    private KycStatus status;

    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdate;
}
