package xy.walletmanagementsystem.infrastructure.output.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import xy.walletmanagementsystem.domain.enums.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "loans",
    indexes = {
        @Index(name = "idx_loans_user_id", columnList = "userId"),
        @Index(name = "idx_loans_status", columnList = "status"),
        @Index(name = "idx_loans_idempotency_key", columnList = "idempotencyKey"),
        @Index(name = "idx_loans_date_created", columnList = "dateCreated")
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private Integer durationInDays;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @Column(columnDefinition = "TEXT")
    private String repaymentSchedule;

    @Column(precision = 19, scale = 2)
    private BigDecimal balanceDue;

    @Column(unique = true)
    private String idempotencyKey;

    private LocalDateTime dateDisbursed;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdate;
}
