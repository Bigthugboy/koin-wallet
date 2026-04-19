package xy.walletmanagementsystem.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.applicationPort.output.NotificationOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.PaymentProviderOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.TransactionOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.WalletOutPutPort;
import xy.walletmanagementsystem.domain.enums.AccountStatus;
import xy.walletmanagementsystem.domain.enums.TransactionStatus;
import xy.walletmanagementsystem.domain.enums.TransactionType;
import xy.walletmanagementsystem.domain.enums.WalletStatus;
import xy.walletmanagementsystem.domain.exception.IdempotencyException;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.PaystackFundingInitResponse;
import xy.walletmanagementsystem.domain.model.PaystackWebhookEvent;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.domain.model.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService")
class WalletServiceTest {

    @Mock private WalletOutPutPort walletOutPutPort;
    @Mock private TransactionOutPutPort transactionOutPutPort;
    @Mock private UserOutPutPort userOutPutPort;
    @Mock private PaymentProviderOutPutPort providerOutPutPort;
    @Mock private NotificationOutPutPort notificationOutPutPort;

    @InjectMocks private WalletService walletService;



    @Nested
    @DisplayName("createWallet")
    class CreateWallet {

        @Test
        @DisplayName("null userId → exception")
        void nullUserId_shouldThrow() {
            assertThrows(WalletManagementException.class, () -> walletService.createWallet(null));
        }

        @Test
        @DisplayName("user not found → exception")
        void userNotFound_shouldThrow() {
            when(userOutPutPort.findById(1L)).thenReturn(Optional.empty());
            assertThrows(WalletManagementException.class, () -> walletService.createWallet(1L));
        }

        @Test
        @DisplayName("suspended account → exception")
        void suspendedAccount_shouldThrow() {
            when(userOutPutPort.findById(1L)).thenReturn(Optional.of(suspendedUser()));
            assertThrows(WalletManagementException.class, () -> walletService.createWallet(1L));
        }

        @Test
        @DisplayName("wallet already exists → exception")
        void walletAlreadyExists_shouldThrow() {
            stubActiveUser(1L);
            when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(wallet(1L, "100")));
            assertThrows(WalletManagementException.class, () -> walletService.createWallet(1L));
        }

        @Test
        @DisplayName("happy path → persists wallet with zero balance in NGN")
        void happyPath_persistsNewWallet() throws Exception {
            stubActiveUser(1L);
            when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.empty());
            when(walletOutPutPort.save(any())).thenAnswer(i -> i.getArgument(0));

            Wallet result = walletService.createWallet(1L);

            assertEquals(1L, result.getUserId());
            assertEquals(BigDecimal.ZERO, result.getBalance());
            assertEquals("NGN", result.getCurrency());
            assertEquals(WalletStatus.ACTIVE, result.getStatus());
        }
    }


    @Nested
    @DisplayName("fundWallet")
    class FundWallet {

        @Test
        @DisplayName("null userId → exception")
        void nullUserId_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> walletService.fundWallet(null, new BigDecimal("50"), "ref", null));
        }

        @Test
        @DisplayName("zero amount → exception")
        void zeroAmount_shouldThrow() {
            stubActiveUser(1L);
            assertThrows(WalletManagementException.class,
                    () -> walletService.fundWallet(1L, BigDecimal.ZERO, "ref", null));
        }

        @Test
        @DisplayName("negative amount → exception")
        void negativeAmount_shouldThrow() {
            stubActiveUser(1L);
            assertThrows(WalletManagementException.class,
                    () -> walletService.fundWallet(1L, new BigDecimal("-1"), "ref", null));
        }

        @Test
        @DisplayName("duplicate idempotency key → IdempotencyException")
        void duplicateIdempotencyKey_shouldThrow() {
            stubActiveUser(1L);
            when(transactionOutPutPort.findByReference("key-123")).thenReturn(Optional.of(pendingTx("key-123")));
            assertThrows(IdempotencyException.class,
                    () -> walletService.fundWallet(1L, new BigDecimal("50"), "ref", "key-123"));
        }

        @Test
        @DisplayName("duplicate reference (no idempotency key) → IdempotencyException")
        void duplicateReference_shouldThrow() {
            stubActiveUser(1L);
            when(transactionOutPutPort.findByReference("ref-1")).thenReturn(Optional.of(pendingTx("ref-1")));
            assertThrows(IdempotencyException.class,
                    () -> walletService.fundWallet(1L, new BigDecimal("50"), "ref-1", null));
        }

        @Test
        @DisplayName("happy path → balance updated and SUCCESSFUL transaction saved")
        void happyPath_updatesBalanceAndSavesTransaction() throws Exception {
            stubActiveUser(1L);
            Wallet w = wallet(1L, "100.00");
            when(transactionOutPutPort.findByReference("ref-1")).thenReturn(Optional.empty());
            when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(w));
            when(walletOutPutPort.save(any())).thenAnswer(i -> i.getArgument(0));

            walletService.fundWallet(1L, new BigDecimal("50.00"), "ref-1", null);

            ArgumentCaptor<Wallet> walletCap = ArgumentCaptor.forClass(Wallet.class);
            verify(walletOutPutPort).save(walletCap.capture());
            assertEquals(0, new BigDecimal("150.00").compareTo(walletCap.getValue().getBalance()));

            ArgumentCaptor<Transaction> txCap = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionOutPutPort).save(txCap.capture());
            assertEquals(TransactionType.CREDIT, txCap.getValue().getType());
            assertEquals(TransactionStatus.SUCCESSFUL, txCap.getValue().getStatus());
        }
    }


    @Nested
    @DisplayName("getWalletBalance")
    class GetWalletBalance {

        @Test
        @DisplayName("null userId → exception")
        void nullUserId_shouldThrow() {
            assertThrows(WalletManagementException.class, () -> walletService.getWalletBalance(null));
        }

        @Test
        @DisplayName("wallet not found → exception")
        void walletNotFound_shouldThrow() {
            when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.empty());
            assertThrows(WalletManagementException.class, () -> walletService.getWalletBalance(1L));
        }

        @Test
        @DisplayName("happy path → returns wallet with correct balance")
        void happyPath_returnsWallet() throws Exception {
            when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(wallet(1L, "250.00")));
            Wallet result = walletService.getWalletBalance(1L);
            assertEquals(0, new BigDecimal("250.00").compareTo(result.getBalance()));
        }
    }


    @Nested
    @DisplayName("getTransactionHistory")
    class GetTransactionHistory {

        @Test
        @DisplayName("null userId → exception")
        void nullUserId_shouldThrow() {
            assertThrows(WalletManagementException.class, () -> walletService.getTransactionHistory(null));
        }

        @Test
        @DisplayName("returns all transactions for user")
        void returnsTransactionsForUser() throws Exception {
            when(transactionOutPutPort.findByUserId(1L))
                    .thenReturn(List.of(pendingTx("r1"), pendingTx("r2")));
            assertEquals(2, walletService.getTransactionHistory(1L).size());
        }
    }

    @Nested
    @DisplayName("initializeFunding")
    class InitializeFunding {

        @Test
        @DisplayName("null user → exception")
        void nullUser_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> walletService.initializeFunding(0L, new BigDecimal("100")));
        }

        @Test
        @DisplayName("user with null id → exception")
        void nullUserId_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> walletService.initializeFunding(0L, new BigDecimal("100")));
        }

        @Test
        @DisplayName("zero amount → exception")
        void zeroAmount_shouldThrow() {
            stubActiveUser(1L);
            assertThrows(WalletManagementException.class,
                    () -> walletService.initializeFunding(activeUser().getId(), BigDecimal.ZERO));
        }

        @Test
        @DisplayName("wallet not found → exception")
        void walletNotFound_shouldThrow() {
            stubActiveUser(1L);
            when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.empty());
            assertThrows(WalletManagementException.class,
                    () -> walletService.initializeFunding(1L, new BigDecimal("100")));
        }

        @Test
        @DisplayName("happy path → saves PENDING transaction and returns Paystack URL")
        void happyPath_savesPendingAndReturnsUrl() throws Exception {
            stubActiveUser(1L);
            when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(wallet(1L, "0")));
            PaystackFundingInitResponse paystackResp = PaystackFundingInitResponse.builder()
                    .authorizationUrl("https://paystack.com/pay/ref")
                    .accessCode("ac_123")
                    .reference("KW-REF001")
                    .build();
            when(providerOutPutPort.initializeTransaction(anyString(), any())).thenReturn(paystackResp);

            PaystackFundingInitResponse result = walletService.initializeFunding(activeUser().getId(), new BigDecimal("100"));

            assertEquals("https://paystack.com/pay/ref", result.getAuthorizationUrl());

            ArgumentCaptor<Transaction> txCap = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionOutPutPort).save(txCap.capture());
            assertEquals(TransactionStatus.PENDING, txCap.getValue().getStatus());
            assertEquals("KW-REF001", txCap.getValue().getReferenceNumber());
        }
    }


    @Nested
    @DisplayName("confirmFunding")
    class ConfirmFunding {

        @Test
        @DisplayName("transaction not found → exception")
        void txNotFound_shouldThrow() {
            when(transactionOutPutPort.findByReference("REF")).thenReturn(Optional.empty());
            assertThrows(WalletManagementException.class,
                    () -> walletService.confirmFunding(webhookEvent("REF", "50")));
        }

        @Test
        @DisplayName("already SUCCESSFUL → idempotency skip, no wallet update")
        void alreadySuccessful_shouldSkip() throws Exception {
            Transaction tx = successfulTx("REF");
            when(transactionOutPutPort.findByReference("REF")).thenReturn(Optional.of(tx));

            walletService.confirmFunding(webhookEvent("REF", "50"));

            verify(walletOutPutPort, never()).save(any());
            verify(transactionOutPutPort, never()).save(any());
        }

        @Test
        @DisplayName("already FAILED → idempotency skip, no wallet update")
        void alreadyFailed_shouldSkip() throws Exception {
            Transaction tx = failedTx("REF");
            when(transactionOutPutPort.findByReference("REF")).thenReturn(Optional.of(tx));

            walletService.confirmFunding(webhookEvent("REF", "50"));

            verify(walletOutPutPort, never()).save(any());
        }

        @Test
        @DisplayName("wallet not found for user → exception")
        void walletNotFound_shouldThrow() {
            when(transactionOutPutPort.findByReference("REF")).thenReturn(Optional.of(pendingTx("REF")));
            when(walletOutPutPort.findByUserId(anyLong())).thenReturn(Optional.empty());
            assertThrows(WalletManagementException.class,
                    () -> walletService.confirmFunding(webhookEvent("REF", "50")));
        }

        @Test
        @DisplayName("happy path → wallet credited, transaction marked SUCCESSFUL, email sent")
        void happyPath_creditsWalletAndMarksSuccessfulAndNotifies() throws Exception {
            Transaction pending = pendingTx("REF");
            Wallet w = wallet(1L, "100.00");
            when(transactionOutPutPort.findByReference("REF")).thenReturn(Optional.of(pending));
            when(walletOutPutPort.findByUserId(pending.getUserId())).thenReturn(Optional.of(w));
            when(walletOutPutPort.save(any())).thenAnswer(i -> i.getArgument(0));

            walletService.confirmFunding(webhookEvent("REF", "75.00"));

            // wallet balance updated
            ArgumentCaptor<Wallet> walletCap = ArgumentCaptor.forClass(Wallet.class);
            verify(walletOutPutPort).save(walletCap.capture());
            assertEquals(0, new BigDecimal("175.00").compareTo(walletCap.getValue().getBalance()));

            // transaction stamped SUCCESSFUL with event description
            ArgumentCaptor<Transaction> txCap = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionOutPutPort).save(txCap.capture());
            assertEquals(TransactionStatus.SUCCESSFUL, txCap.getValue().getStatus());
            assertEquals("charge.success", txCap.getValue().getDescription());

            // payment notification email sent
            verify(notificationOutPutPort).sendPaymentNotification(
                    eq("user@test.com"), contains("REF"));
        }

        @Test
        @DisplayName("already terminal → idempotency skip, no notification sent")
        void alreadyTerminal_shouldSkip_andNoEmail() throws Exception {
            when(transactionOutPutPort.findByReference("REF")).thenReturn(Optional.of(successfulTx("REF")));

            walletService.confirmFunding(webhookEvent("REF", "50"));

            verify(notificationOutPutPort, never()).sendPaymentNotification(any(), any());
        }
    }



    @Nested
    @DisplayName("markTransactionTerminal")
    class MarkTransactionTerminal {

        @Test
        @DisplayName("blank reference → exception")
        void blankReference_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> walletService.markTransactionTerminal("  ", TransactionStatus.FAILED, "charge.failed"));
        }

        @Test
        @DisplayName("transaction not found → exception")
        void txNotFound_shouldThrow() {
            when(transactionOutPutPort.findByReference("REF")).thenReturn(Optional.empty());
            assertThrows(WalletManagementException.class,
                    () -> walletService.markTransactionTerminal("REF", TransactionStatus.FAILED, "charge.failed"));
        }

        @Test
        @DisplayName("already terminal → idempotency skip")
        void alreadyTerminal_shouldSkip() throws Exception {
            when(transactionOutPutPort.findByReference("REF")).thenReturn(Optional.of(successfulTx("REF")));
            walletService.markTransactionTerminal("REF", TransactionStatus.FAILED, "charge.failed");
            verify(transactionOutPutPort, never()).save(any());
        }

        @Test
        @DisplayName("FAILED terminal state saved with event description")
        void failedState_savedCorrectly() throws Exception {
            when(transactionOutPutPort.findByReference("REF")).thenReturn(Optional.of(pendingTx("REF")));

            walletService.markTransactionTerminal("REF", TransactionStatus.FAILED, "charge.failed");

            ArgumentCaptor<Transaction> cap = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionOutPutPort).save(cap.capture());
            assertEquals(TransactionStatus.FAILED, cap.getValue().getStatus());
            assertEquals("charge.failed", cap.getValue().getDescription());
        }

        @Test
        @DisplayName("REVERSED terminal state saved with event description")
        void reversedState_savedCorrectly() throws Exception {
            when(transactionOutPutPort.findByReference("REF")).thenReturn(Optional.of(pendingTx("REF")));

            walletService.markTransactionTerminal("REF", TransactionStatus.REVERSED, "transfer.reversed");

            ArgumentCaptor<Transaction> cap = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionOutPutPort).save(cap.capture());
            assertEquals(TransactionStatus.REVERSED, cap.getValue().getStatus());
            assertEquals("transfer.reversed", cap.getValue().getDescription());
        }

        @Test
        @DisplayName("no wallet balance change on any terminal non-credit event")
        void noWalletChange_onTerminalEvent() throws Exception {
            when(transactionOutPutPort.findByReference("REF")).thenReturn(Optional.of(pendingTx("REF")));
            walletService.markTransactionTerminal("REF", TransactionStatus.FAILED, "transfer.failed");
            verifyNoInteractions(walletOutPutPort);
        }
    }



    private void stubActiveUser(Long id) {
        when(userOutPutPort.findById(id)).thenReturn(Optional.of(activeUser()));
    }

    private User activeUser() {
        return User.builder().id(1L).email("user@test.com")
                .status(AccountStatus.ACTIVE).build();
    }

    private User suspendedUser() {
        return User.builder().id(1L).status(AccountStatus.SUSPENDED).build();
    }

    private Wallet wallet(Long userId, String balance) {
        return Wallet.builder().walletId(10L).userId(userId)
                .balance(new BigDecimal(balance)).status(WalletStatus.ACTIVE).build();
    }

    private Transaction pendingTx(String ref) {
        return Transaction.builder().transactionId(99L).userId(1L).walletId(10L)
                .referenceNumber(ref).status(TransactionStatus.PENDING)
                .amount(new BigDecimal("50")).type(TransactionType.CREDIT).build();
    }

    private Transaction successfulTx(String ref) {
        return Transaction.builder().transactionId(99L).userId(1L).walletId(10L)
                .referenceNumber(ref).status(TransactionStatus.SUCCESSFUL)
                .amount(new BigDecimal("50")).type(TransactionType.CREDIT).build();
    }

    private Transaction failedTx(String ref) {
        return Transaction.builder().transactionId(99L).userId(1L).walletId(10L)
                .referenceNumber(ref).status(TransactionStatus.FAILED)
                .amount(new BigDecimal("50")).type(TransactionType.CREDIT).build();
    }

    private PaystackWebhookEvent webhookEvent(String ref, String amount) {
        return PaystackWebhookEvent.builder()
                .event("charge.success")
                .reference(ref)
                .amount(new BigDecimal(amount))
                .customerEmail("user@test.com")
                .paystackStatus("success")
                .build();
    }
}
