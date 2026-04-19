package xy.walletmanagementsystem.infrastructure.output.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import xy.walletmanagementsystem.domain.enums.KycStatus;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "kyc_details",
    indexes = {
        @Index(name = "idx_kyc_user_id", columnList = "userId"),
        @Index(name = "idx_kyc_bvn", columnList = "bvn"),
        @Index(name = "idx_kyc_nin", columnList = "nin"),
        @Index(name = "idx_kyc_status", columnList = "status")
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycEntity {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(unique = true)
    private String bvn;

    @Column(unique = true)
    private String nin;

    @Enumerated(EnumType.STRING)
    private KycStatus status;

    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdate;
}
