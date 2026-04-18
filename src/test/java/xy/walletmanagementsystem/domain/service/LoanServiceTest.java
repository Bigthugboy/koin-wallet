package xy.walletmanagementsystem.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.applicationPort.output.LoanOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.NotificationOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.TransactionOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.WalletOutPutPort;
import xy.walletmanagementsystem.domain.enums.LoanStatus;
import xy.walletmanagementsystem.domain.enums.TransactionType;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Loan;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.domain.model.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanOutPutPort loanOutPutPort;
    @Mock
    private WalletOutPutPort walletOutPutPort;
    @Mock
    private TransactionOutPutPort transactionOutPutPort;
    @Mock
    private UserOutPutPort userOutPutPort;
    @Mock
    private NotificationOutPutPort notificationOutPutPort;

    @InjectMocks
    private LoanService loanService;

    @Test
    void applyForLoan_shouldRejectWhenWalletNotFunded() {
        Wallet wallet = Wallet.builder().userId(1L).balance(BigDecimal.ZERO).build();
        when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(wallet));

        assertThrows(WalletManagementException.class,
                () -> loanService.applyForLoan(1L, new BigDecimal("100"), 30));
    }

    @Test
    void applyForLoan_shouldRejectWhenAmountExceedsThreeXWalletBalance() {
        Wallet wallet = Wallet.builder().userId(1L).balance(new BigDecimal("100")).build();
        when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(wallet));

        assertThrows(WalletManagementException.class,
                () -> loanService.applyForLoan(1L, new BigDecimal("301"), 30));
    }

    @Test
    void applyForLoan_shouldCreatePendingLoan() throws Exception {
        Wallet wallet = Wallet.builder().userId(1L).balance(new BigDecimal("200")).build();
        User user = User.builder().id(1L).email("john@example.com").build();
        when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(userOutPutPort.findById(1L)).thenReturn(Optional.of(user));
        when(loanOutPutPort.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan loan = loanService.applyForLoan(1L, new BigDecimal("300"), 15);

        assertEquals(LoanStatus.PENDING, loan.getStatus());
        assertEquals(new BigDecimal("5.0"), loan.getInterestRate());
    }

    @Test
    void approveLoan_shouldRejectIfNotPending() {
        Loan loan = Loan.builder().loanId(1L).status(LoanStatus.APPROVED).build();
        when(loanOutPutPort.findById(1L)).thenReturn(Optional.of(loan));

        assertThrows(WalletManagementException.class, () -> loanService.approveLoan(1L));
    }

    @Test
    void disburseLoan_shouldUpdateWalletLoanAndLogTransaction() throws Exception {
        Loan loan = Loan.builder()
                .loanId(1L)
                .userId(1L)
                .amount(new BigDecimal("100"))
                .status(LoanStatus.APPROVED)
                .build();
        Wallet wallet = Wallet.builder()
                .walletId(1L)
                .userId(1L)
                .balance(new BigDecimal("50"))
                .build();
        User user = User.builder().id(1L).email("john@example.com").build();
        when(loanOutPutPort.findById(1L)).thenReturn(Optional.of(loan));
        when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(userOutPutPort.findById(1L)).thenReturn(Optional.of(user));
        when(walletOutPutPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanOutPutPort.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan disbursed = loanService.disburseLoan(1L);

        assertEquals(LoanStatus.DISBURSED, disbursed.getStatus());
        assertNotNull(disbursed.getDateDisbursed());
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionOutPutPort).save(txCaptor.capture());
        assertEquals(TransactionType.LOAN_DISBURSEMENT, txCaptor.getValue().getType());
    }

    @Test
    void repayLoan_shouldRejectWhenInsufficientFunds() {
        String idempotencyKey = "repay-1";
        Loan loan = Loan.builder().loanId(1L).userId(1L).status(LoanStatus.DISBURSED).build();
        Wallet wallet = Wallet.builder().userId(1L).balance(new BigDecimal("10")).build();
        when(loanOutPutPort.findById(1L)).thenReturn(Optional.of(loan));
        when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(wallet));

        assertThrows(WalletManagementException.class,
                () -> loanService.repayLoan(1L, new BigDecimal("50"), idempotencyKey));
    }

    @Test
    void repayLoan_shouldDeductWalletAndMarkLoanRepaid() throws Exception {
        Loan loan = Loan.builder().loanId(1L).userId(1L).status(LoanStatus.DISBURSED).build();
        Wallet wallet = Wallet.builder().walletId(1L).userId(1L).balance(new BigDecimal("100")).build();
        User user = User.builder().id(1L).email("john@example.com").build();
        when(loanOutPutPort.findById(1L)).thenReturn(Optional.of(loan));
        when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(userOutPutPort.findById(1L)).thenReturn(Optional.of(user));
        when(walletOutPutPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanOutPutPort.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        loanService.repayLoan(1L, new BigDecimal("30"), "repay-1");

        assertEquals(LoanStatus.REPAID, loan.getStatus());
        verify(transactionOutPutPort).save(any(Transaction.class));
    }

    @Test
    void listAllLoans_shouldReturnAllRecords() throws Exception {
        when(loanOutPutPort.findAll()).thenReturn(List.of(Loan.builder().loanId(1L).build()));
        List<Loan> loans = loanService.listAllLoans();
        assertEquals(1, loans.size());
    }
}
