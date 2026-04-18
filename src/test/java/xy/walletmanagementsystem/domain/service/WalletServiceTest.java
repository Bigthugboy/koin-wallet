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
        assertThrows(WalletManagementException.class, () -> walletService.createWallet(""));
    }

    @Test
    void createWallet_shouldFailWhenWalletExists() {
        when(walletOutPutPort.findByUserId("user-1")).thenReturn(Optional.of(Wallet.builder().build()));
        assertThrows(WalletManagementException.class, () -> walletService.createWallet("user-1"));
    }

    @Test
    void createWallet_shouldPersistDefaultWallet() throws Exception {
        when(walletOutPutPort.findByUserId("user-1")).thenReturn(Optional.empty());
        when(walletOutPutPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Wallet wallet = walletService.createWallet("user-1");

        assertEquals("user-1", wallet.getUserId());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
        assertEquals("NGN", wallet.getCurrency());
        assertEquals(WalletStatus.ACTIVE, wallet.getStatus());
    }

    @Test
    void fundWallet_shouldUpdateBalanceAndCreateTransaction() throws Exception {
        Wallet wallet = Wallet.builder()
                .walletId("wallet-1")
                .userId("user-1")
                .balance(new BigDecimal("100.00"))
                .build();
        when(walletOutPutPort.findByUserId("user-1")).thenReturn(Optional.of(wallet));
        when(walletOutPutPort.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        walletService.fundWallet("user-1", new BigDecimal("50.00"), "ref-1");

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionOutPutPort).save(txCaptor.capture());
        assertEquals(TransactionType.CREDIT, txCaptor.getValue().getType());
        assertEquals(new BigDecimal("50.00"), txCaptor.getValue().getAmount());
    }

    @Test
    void fundWallet_shouldRejectInvalidAmount() {
        assertThrows(WalletManagementException.class,
                () -> walletService.fundWallet("user-1", BigDecimal.ZERO, "ref-1"));
    }

    @Test
    void getWalletBalance_shouldReturnWallet() throws Exception {
        Wallet wallet = Wallet.builder().userId("user-1").balance(BigDecimal.TEN).build();
        when(walletOutPutPort.findByUserId("user-1")).thenReturn(Optional.of(wallet));

        Wallet result = walletService.getWalletBalance("user-1");
        assertEquals(BigDecimal.TEN, result.getBalance());
    }

    @Test
    void getTransactionHistory_shouldReturnTransactions() throws Exception {
        List<Transaction> transactions = List.of(Transaction.builder().transactionId("tx-1").build());
        when(transactionOutPutPort.findByUserId("user-1")).thenReturn(transactions);

        List<Transaction> result = walletService.getTransactionHistory("user-1");
        assertEquals(1, result.size());
    }
}
