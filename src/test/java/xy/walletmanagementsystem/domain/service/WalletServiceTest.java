package xy.walletmanagementsystem.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.applicationPort.output.TransactionOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.WalletOutPutPort;
import xy.walletmanagementsystem.domain.enums.TransactionType;
import xy.walletmanagementsystem.domain.enums.WalletStatus;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletOutPutPort walletOutPutPort;
    @Mock
    private TransactionOutPutPort transactionOutPutPort;

    @InjectMocks
    private WalletService walletService;

    @Test
    void createWallet_shouldFailWhenUserIdBlank() {
        assertThrows(WalletManagementException.class, () -> walletService.createWallet(null));
    }

    @Test
    void createWallet_shouldFailWhenWalletExists() {
        when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(Wallet.builder().build()));
        assertThrows(WalletManagementException.class, () -> walletService.createWallet(1L));
    }

    @Test
    void createWallet_shouldPersistDefaultWallet() throws Exception {
        when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.empty());
        when(walletOutPutPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Wallet wallet = walletService.createWallet(1L);

        assertEquals(1L, wallet.getUserId());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
        assertEquals("NGN", wallet.getCurrency());
        assertEquals(WalletStatus.ACTIVE, wallet.getStatus());
    }

    @Test
    void fundWallet_shouldUpdateBalanceAndCreateTransaction() throws Exception {
        Wallet wallet = Wallet.builder()
                .walletId(1L)
                .userId(1L)
                .balance(new BigDecimal("100.00"))
                .build();
        when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletOutPutPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        walletService.fundWallet(1L, new BigDecimal("50.00"), "ref-1", null);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionOutPutPort).save(txCaptor.capture());
        assertEquals(TransactionType.CREDIT, txCaptor.getValue().getType());
        assertEquals(new BigDecimal("50.00"), txCaptor.getValue().getAmount());
    }

    @Test
    void fundWallet_shouldRejectInvalidAmount() {
        assertThrows(WalletManagementException.class,
                () -> walletService.fundWallet(1L, BigDecimal.ZERO, "ref-1", null));
    }

    @Test
    void fundWallet_shouldRejectDuplicateIdempotencyKey() {
        when(transactionOutPutPort.findByReference("idemp-key-123")).thenReturn(Optional.of(Transaction.builder().build()));
        assertThrows(xy.walletmanagementsystem.domain.exception.IdempotencyException.class,
                () -> walletService.fundWallet(1L, new BigDecimal("50.00"), "ref-1", "idemp-key-123"));
    }

    @Test
    void getWalletBalance_shouldReturnWallet() throws Exception {
        Wallet wallet = Wallet.builder().userId(1L).balance(BigDecimal.TEN).build();
        when(walletOutPutPort.findByUserId(1L)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.getWalletBalance(1L);
        assertEquals(BigDecimal.TEN, result.getBalance());
    }

    @Test
    void getTransactionHistory_shouldReturnTransactions() throws Exception {
        List<Transaction> transactions = List.of(Transaction.builder().transactionId(1L).build());
        when(transactionOutPutPort.findByUserId(1L)).thenReturn(transactions);

        List<Transaction> result = walletService.getTransactionHistory(1L);
        assertEquals(1, result.size());
    }
}
