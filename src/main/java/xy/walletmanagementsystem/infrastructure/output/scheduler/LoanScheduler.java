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
import xy.walletmanagementsystem.domain.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanScheduler {

    private final LoanOutPutPort loanOutPutPort;
    private final NotificationOutPutPort notificationOutPutPort;
    private final UserOutPutPort userOutPutPort;

    /**
     * Runs daily at 8 AM to send reminders for loans due in 3 days.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendRepaymentReminders() {
        log.info("Running loan repayment reminders job...");
        List<Loan> disbursedLoans = loanOutPutPort.findAll().stream()
                .filter(loan -> loan.getStatus() == LoanStatus.DISBURSED)
                .toList();

        LocalDateTime reminderThreshold = LocalDateTime.now().plusDays(3);

        for (Loan loan : disbursedLoans) {
            // Logic to check if due date is within threshold
            // Simplified for this simulation
            userOutPutPort.findById(loan.getUserId()).ifPresent(user -> {
                notificationOutPutPort.sendLoanNotification(user.getEmail(), 
                        "Reminder: Your loan of " + loan.getAmount() + " is due soon.");
            });
        }
    }

    /**
     * Runs daily at midnight to mark overdue loans.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdueLoans() {
        log.info("Running overdue loans job...");
        List<Loan> disbursedLoans = loanOutPutPort.findAll().stream()
                .filter(loan -> loan.getStatus() == LoanStatus.DISBURSED)
                .toList();

        LocalDateTime now = LocalDateTime.now();

        for (Loan loan : disbursedLoans) {
            // Simplified: if createdDate + duration < now, mark defaulted
            if (loan.getCreatedDate().plusDays(loan.getDurationInDays()).isBefore(now)) {
                log.warn("Marking loan {} as DEFAULTED", loan.getLoanId());
                loan.setStatus(LoanStatus.DEFAULTED);
                loan.setUpdatedDate(now);
                loanOutPutPort.save(loan);
            }
        }
    }
}
