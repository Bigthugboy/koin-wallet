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
@Table(name = "loans")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanEntity {
    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private String userId;

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

    private LocalDateTime dateDisbursed;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdate;
}
