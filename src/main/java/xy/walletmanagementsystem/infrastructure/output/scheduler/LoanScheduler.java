package xy.walletmanagementsystem.infrastructure.output.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xy.walletmanagementsystem.applicationPort.output.LoanOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.NotificationOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.enums.LoanStatus;
import xy.walletmanagementsystem.domain.model.Loan;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanScheduler {

    private final LoanOutPutPort loanOutPutPort;
    private final NotificationOutPutPort notificationOutPutPort;
    private final UserOutPutPort userOutPutPort;


     // Runs daily at 8 AM to send reminders for loans due in 3 days.

    @Scheduled(cron = "0 0 8 * * ?")
    public void sendRepaymentReminders() {
        log.info("Running loan repayment reminders job...");
        List<Loan> disbursedLoans = loanOutPutPort.findAll().stream()
                .filter(loan -> loan.getStatus() == LoanStatus.DISBURSED)
                .toList();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTargetDate = now.plusDays(3);

        for (Loan loan : disbursedLoans) {
            LocalDateTime dueDate = calculateDueDate(loan);
            if (dueDate.toLocalDate().isEqual(reminderTargetDate.toLocalDate())) {
                userOutPutPort.findById(loan.getUserId()).ifPresent(user ->
                        notificationOutPutPort.sendLoanNotification(
                                user.getEmail(),
                                "Reminder: Your loan repayment of " + loan.getAmount() + " is due on " + dueDate.toLocalDate()
                        ));
            }
        }
    }


      //Runs daily at midnight to mark overdue loans.

    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdueLoans() {
        log.info("Running overdue loans job...");
        List<Loan> disbursedLoans = loanOutPutPort.findAll().stream()
                .filter(loan -> loan.getStatus() == LoanStatus.DISBURSED)
                .toList();

        LocalDateTime now = LocalDateTime.now();

        for (Loan loan : disbursedLoans) {
            LocalDateTime dueDate = calculateDueDate(loan);
            if (dueDate.isBefore(now)) {
                log.warn("Marking loan {} as DEFAULTED", loan.getLoanId());
                loan.setStatus(LoanStatus.DEFAULTED);
                loan.setDateUpdate(now);
                loanOutPutPort.save(loan);
            }
        }
    }

    private LocalDateTime calculateDueDate(Loan loan) {
        LocalDateTime disbursementDate = loan.getDateDisbursed() != null
                ? loan.getDateDisbursed()
                : (loan.getDateUpdate() != null ? loan.getDateUpdate() : loan.getDateCreated());
        return disbursementDate.plusDays(loan.getDurationInDays());
    }
}
