package xy.walletmanagementsystem.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.output.PaymentProviderOutPutPort;
import xy.walletmanagementsystem.domain.enums.TransactionStatus;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.PaystackWebhookEvent;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookService")
class WebhookServiceTest {

    @Mock private WalletUseCase walletUseCase;
    @Mock private PaymentProviderOutPutPort paymentProviderOutPutPort;
    @InjectMocks private WebhookService webhookService;

    @BeforeEach
    void injectObjectMapper() throws Exception {
        var field = WebhookService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(webhookService, new ObjectMapper());
    }



    @Test
    @DisplayName("null signature → rejected with exception")
    void nullSignature_shouldReject() {
        assertThrows(WalletManagementException.class,
                () -> webhookService.processRawWebhook(chargeSuccessBody("r", 5000, "a@b.com"), null));
        verifyNoInteractions(walletUseCase);
    }

    @Test
    @DisplayName("invalid HMAC → rejected with exception")
    void invalidSignature_shouldReject() {
        when(paymentProviderOutPutPort.verifyWebhookSignature(any(), any())).thenReturn(false);
        assertThrows(WalletManagementException.class,
                () -> webhookService.processRawWebhook(chargeSuccessBody("r", 5000, "a@b.com"), "bad"));
        verifyNoInteractions(walletUseCase);
    }


    @Test
    @DisplayName("malformed JSON → WalletManagementException")
    void malformedJson_shouldThrow() {
        assertThrows(WalletManagementException.class,
                () -> webhookService.parsePaystackEvent("not-json"));
    }

    @Test
    @DisplayName("missing 'event' field → WalletManagementException")
    void missingEventField_shouldThrow() {
        String body = """
            {"data":{"reference":"r","status":"success","amount":5000,"customer":{"email":"x@y.com"}}}""";
        assertThrows(WalletManagementException.class, () -> webhookService.parsePaystackEvent(body));
    }

    @Test
    @DisplayName("missing 'data' block → WalletManagementException")
    void missingDataBlock_shouldThrow() {
        assertThrows(WalletManagementException.class,
                () -> webhookService.parsePaystackEvent("{\"event\":\"charge.success\"}"));
    }

    @Test
    @DisplayName("missing customer email → WalletManagementException")
    void missingCustomerEmail_shouldThrow() {
        String body = """
            {"event":"charge.success","data":{"reference":"r","status":"success","amount":5000,"customer":{}}}""";
        assertThrows(WalletManagementException.class, () -> webhookService.parsePaystackEvent(body));
    }

    @Test
    @DisplayName("missing reference → WalletManagementException")
    void missingReference_shouldThrow() {
        String body = """
            {"event":"charge.success","data":{"status":"success","amount":5000,"customer":{"email":"x@y.com"}}}""";
        assertThrows(WalletManagementException.class, () -> webhookService.parsePaystackEvent(body));
    }

    @Test
    @DisplayName("amount 5500 kobo → 55 naira conversion")
    void amountConversion_koboToNaira() throws Exception {
        PaystackWebhookEvent event = webhookService.parsePaystackEvent(chargeSuccessBody("r", 5500, "x@y.com"));
        assertEquals(0, new BigDecimal("55").compareTo(event.getAmount()));
    }

    @Test
    @DisplayName("valid charge.success body parses all fields")
    void validBody_parsesAllFields() throws Exception {
        PaystackWebhookEvent event = webhookService.parsePaystackEvent(chargeSuccessBody("REF-1", 10000, "john@example.com"));
        assertEquals("charge.success", event.getEvent());
        assertEquals("REF-1", event.getReference());
        assertEquals("john@example.com", event.getCustomerEmail());
        assertEquals(0, new BigDecimal("100").compareTo(event.getAmount()));
        assertEquals("success", event.getPaystackStatus());
    }



    @Nested
    @DisplayName("Event routing (valid signature)")
    class EventRouting {

        @BeforeEach
        void stubSig() {
            when(paymentProviderOutPutPort.verifyWebhookSignature(any(), any())).thenReturn(true);
        }

        @Test
        @DisplayName("charge.success → confirmFunding called with correct event")
        void chargeSuccess_callsConfirmFunding() throws Exception {
            webhookService.processRawWebhook(chargeSuccessBody("r1", 5000, "a@b.com"), "sig");
            ArgumentCaptor<PaystackWebhookEvent> cap = ArgumentCaptor.forClass(PaystackWebhookEvent.class);
            verify(walletUseCase).confirmFunding(cap.capture());
            assertEquals("r1", cap.getValue().getReference());
            assertEquals(0, new BigDecimal("50").compareTo(cap.getValue().getAmount()));
        }

        @Test
        @DisplayName("charge.failed → markTransactionTerminal FAILED")
        void chargeFailed_marksTransactionFailed() throws Exception {
            webhookService.processRawWebhook(eventBody("charge.failed", "r2", "failed", "a@b.com"), "sig");
            verify(walletUseCase).markTransactionTerminal("r2", TransactionStatus.FAILED, "charge.failed");
            verify(walletUseCase, never()).confirmFunding(any());
        }

        @Test
        @DisplayName("transfer.success → markTransactionTerminal SUCCESSFUL")
        void transferSuccess_marksSuccessful() throws Exception {
            webhookService.processRawWebhook(eventBody("transfer.success", "r3", "success", "a@b.com"), "sig");
            verify(walletUseCase).markTransactionTerminal("r3", TransactionStatus.SUCCESSFUL, "transfer.success");
        }

        @Test
        @DisplayName("transfer.failed → markTransactionTerminal FAILED")
        void transferFailed_marksFailed() throws Exception {
            webhookService.processRawWebhook(eventBody("transfer.failed", "r4", "failed", "a@b.com"), "sig");
            verify(walletUseCase).markTransactionTerminal("r4", TransactionStatus.FAILED, "transfer.failed");
        }

        @Test
        @DisplayName("transfer.reversed → markTransactionTerminal REVERSED")
        void transferReversed_marksReversed() throws Exception {
            webhookService.processRawWebhook(eventBody("transfer.reversed", "r5", "reversed", "a@b.com"), "sig");
            verify(walletUseCase).markTransactionTerminal("r5", TransactionStatus.REVERSED, "transfer.reversed");
        }

        @Test
        @DisplayName("unknown event type → ignored, no wallet interaction")
        void unknownEvent_silentlyIgnored() throws Exception {
            String body = """
                {"event":"subscription.create","data":{"reference":"r6","status":"active",
                 "amount":1000,"customer":{"email":"a@b.com"}}}""";
            webhookService.processRawWebhook(body, "sig");
            verifyNoInteractions(walletUseCase);
        }
    }



    private String chargeSuccessBody(String ref, int kobo, String email) {
        return eventBody("charge.success", ref, "success", email, kobo);
    }

    private String eventBody(String event, String ref, String status, String email) {
        return eventBody(event, ref, status, email, 5000);
    }

    private String eventBody(String event, String ref, String status, String email, int kobo) {
        return """
            {"event":"%s","data":{"reference":"%s","status":"%s","amount":%d,"customer":{"email":"%s"}}}
            """.formatted(event, ref, status, kobo, email);
    }
}
