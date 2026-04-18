package xy.walletmanagementsystem.infrastructure.output.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.applicationPort.output.LoanOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.NotificationOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.enums.LoanStatus;
import xy.walletmanagementsystem.domain.model.Loan;
import xy.walletmanagementsystem.domain.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanSchedulerTest {

    @Mock
    private LoanOutPutPort loanOutPutPort;
    @Mock
    private NotificationOutPutPort notificationOutPutPort;
    @Mock
    private UserOutPutPort userOutPutPort;

    @InjectMocks
    private LoanScheduler loanScheduler;

    @Test
    void sendRepaymentReminders_shouldNotifyForLoansDueInThreeDays() {
        LocalDateTime now = LocalDateTime.now();
        Loan dueSoon = Loan.builder()
                .loanId("loan-1")
                .userId("user-1")
                .status(LoanStatus.DISBURSED)
                .durationInDays(10)
                .updatedDate(now.minusDays(7))
                .createdDate(now.minusDays(8))
                .build();
        Loan notDueSoon = Loan.builder()
                .loanId("loan-2")
                .userId("user-2")
                .status(LoanStatus.DISBURSED)
                .durationInDays(10)
                .updatedDate(now.minusDays(2))
                .createdDate(now.minusDays(2))
                .build();

        when(loanOutPutPort.findAll()).thenReturn(List.of(dueSoon, notDueSoon));
        when(userOutPutPort.findById("user-1"))
                .thenReturn(Optional.of(User.builder().id("user-1").email("john@example.com").build()));

        loanScheduler.sendRepaymentReminders();

        verify(notificationOutPutPort).sendLoanNotification(eq("john@example.com"), contains("due on"));
        verify(notificationOutPutPort, never()).sendLoanNotification(eq("user2@example.com"), any());
    }

    @Test
    void markOverdueLoans_shouldSetStatusToDefaultedWhenPastDue() {
        LocalDateTime now = LocalDateTime.now();
        Loan overdueLoan = Loan.builder()
                .loanId("loan-1")
                .userId("user-1")
                .status(LoanStatus.DISBURSED)
                .durationInDays(5)
                .updatedDate(now.minusDays(6))
                .createdDate(now.minusDays(7))
                .build();
        Loan activeLoan = Loan.builder()
                .loanId("loan-2")
                .userId("user-2")
                .status(LoanStatus.DISBURSED)
                .durationInDays(10)
                .updatedDate(now.minusDays(3))
                .createdDate(now.minusDays(3))
                .build();

        when(loanOutPutPort.findAll()).thenReturn(List.of(overdueLoan, activeLoan));
        when(loanOutPutPort.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        loanScheduler.markOverdueLoans();

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        verify(loanOutPutPort).save(loanCaptor.capture());
        assertEquals(LoanStatus.DEFAULTED, loanCaptor.getValue().getStatus());
        assertEquals("loan-1", loanCaptor.getValue().getLoanId());
    }
}
