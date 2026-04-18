package xy.walletmanagementsystem.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.output.NotificationOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.User;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private WalletUseCase walletUseCase;
    @Mock
    private UserOutPutPort userOutPutPort;
    @Mock
    private NotificationOutPutPort notificationOutPutPort;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    void handlePaystackWebhook_shouldFundWalletWhenPaymentSuccessful() throws Exception {
        User user = User.builder().id(1L).email("john@example.com").build();
        when(userOutPutPort.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Map<String, Object> payload = Map.of(
                "data", Map.of(
                        "status", "success",
                        "amount", 5500,
                        "reference", "ref-1",
                        "customer", Map.of("email", "john@example.com")
                )
        );

        String response = webhookService.handlePaystackWebhook("sig", payload);

        assertEquals("Webhook processed", response);
        verify(walletUseCase).fundWallet(eq(1L), eq(new BigDecimal("55")), eq("ref-1"));
        verify(notificationOutPutPort).sendPaymentNotification(eq("john@example.com"), contains("ref-1"));
    }

    @Test
    void handlePaystackWebhook_shouldAcknowledgeNonSuccessStatus() throws Exception {
        Map<String, Object> payload = Map.of("data", Map.of("status", "failed"));
        String response = webhookService.handlePaystackWebhook("sig", payload);
        assertEquals("Webhook received", response);
    }

    @Test
    void handlePaystackWebhook_shouldFailOnInvalidPayload() {
        Map<String, Object> payload = Map.of("event", "charge.success");
        assertThrows(WalletManagementException.class,
                () -> webhookService.handlePaystackWebhook("sig", payload));
    }
}
