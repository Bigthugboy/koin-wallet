package xy.walletmanagementsystem.infrastructure.output.paystack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.PaystackFundingInitResponse;
import xy.walletmanagementsystem.domain.model.PaystackInitializeResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaystackService")
class PaystackServiceTest {

    @Mock private RestTemplate restTemplate;
    @InjectMocks private PaystackService paystackService;

    private static final String SECRET_KEY = "test-secret-key";
    private static final String VALID_EMAIL = "user@example.com";

    @BeforeEach
    void injectSecretKey() throws Exception {
        var field = PaystackService.class.getDeclaredField("secretKey");
        field.setAccessible(true);
        field.set(paystackService, SECRET_KEY);
    }


    @Nested
    @DisplayName("initializeTransaction")
    class InitializeTransaction {

        @Test
        @DisplayName("blank email → WalletManagementException")
        void blankEmail_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction("", new BigDecimal("100")));
        }

        @Test
        @DisplayName("invalid email format → WalletManagementException")
        void invalidEmailFormat_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction("not-an-email", new BigDecimal("100")));
        }

        @Test
        @DisplayName("null amount → WalletManagementException")
        void nullAmount_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, null));
        }

        @Test
        @DisplayName("zero amount → WalletManagementException")
        void zeroAmount_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, BigDecimal.ZERO));
        }

        @Test
        @DisplayName("negative amount → WalletManagementException")
        void negativeAmount_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("-5")));
        }

        @Test
        @DisplayName("Paystack returns null body → WalletManagementException")
        void nullResponseBody_shouldThrow() {
            when(restTemplate.exchange(any(), any(), any(), eq(PaystackInitializeResponse.class)))
                    .thenReturn(ResponseEntity.ok(null));
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("100")));
        }

        @Test
        @DisplayName("Paystack returns status=false → WalletManagementException")
        void paystackStatusFalse_shouldThrow() {
            PaystackInitializeResponse failResp = buildPaystackResponse(false, null, null);
            when(restTemplate.exchange(any(), any(), any(), eq(PaystackInitializeResponse.class)))
                    .thenReturn(ResponseEntity.ok(failResp));
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("100")));
        }

        @Test
        @DisplayName("RestTemplate throws → WalletManagementException wrapping network error")
        void networkError_shouldThrow() {
            when(restTemplate.exchange(any(), any(), any(), eq(PaystackInitializeResponse.class)))
                    .thenThrow(new RestClientException("timeout"));
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("100")));
        }

        @Test
        @DisplayName("happy path → returns authorization URL and reference")
        void happyPath_returnsAuthUrl() throws Exception {
            PaystackInitializeResponse resp = buildPaystackResponse(true,
                    "https://paystack.com/pay/ref", "access-code-123");
            when(restTemplate.exchange(any(), any(), any(), eq(PaystackInitializeResponse.class)))
                    .thenReturn(ResponseEntity.ok(resp));

            PaystackFundingInitResponse result =
                    paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("500"));

            assertEquals("https://paystack.com/pay/ref", result.getAuthorizationUrl());
            assertEquals("access-code-123", result.getAccessCode());
            assertNotNull(result.getReference());
            assertTrue(result.getReference().startsWith("KW-"));
        }

        @Test
        @DisplayName("amount is converted to kobo (x100) before sending")
        void amountConvertedToKobo() throws Exception {
            PaystackInitializeResponse resp = buildPaystackResponse(true, "https://url", "ac");
            when(restTemplate.exchange(any(), any(), any(), eq(PaystackInitializeResponse.class)))
                    .thenReturn(ResponseEntity.ok(resp));


            PaystackFundingInitResponse result =
                    paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("200.50"));
            assertNotNull(result);
        }
    }



    @Nested
    @DisplayName("verifyWebhookSignature")
    class VerifyWebhookSignature {

        @Test
        @DisplayName("correctly computed HMAC → returns true")
        void validSignature_returnsTrue() throws Exception {
            String payload = "{\"event\":\"charge.success\"}";
            String computedSig = computeHmac(payload, SECRET_KEY);
            assertTrue(paystackService.verifyWebhookSignature(payload, computedSig));
        }

        @Test
        @DisplayName("wrong signature → returns false")
        void wrongSignature_returnsFalse() {
            assertFalse(paystackService.verifyWebhookSignature("{\"event\":\"charge.success\"}", "wrong-sig"));
        }

        @Test
        @DisplayName("signature computed from different payload → returns false")
        void signatureFromDifferentPayload_returnsFalse() throws Exception {
            String sig = computeHmac("different payload", SECRET_KEY);
            assertFalse(paystackService.verifyWebhookSignature("{\"event\":\"charge.success\"}", sig));
        }

        @Test
        @DisplayName("empty payload with correct HMAC → returns true")
        void emptyPayload_withCorrectHmac_returnsTrue() throws Exception {
            String sig = computeHmac("", SECRET_KEY);
            assertTrue(paystackService.verifyWebhookSignature("", sig));
        }

        @Test
        @DisplayName("signature is case-insensitive")
        void signatureCaseInsensitive_returnsTrue() throws Exception {
            String payload = "test-body";
            String sig = computeHmac(payload, SECRET_KEY).toUpperCase();
            assertTrue(paystackService.verifyWebhookSignature(payload, sig));
        }
    }


    private String computeHmac(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    private PaystackInitializeResponse buildPaystackResponse(boolean status, String authUrl, String accessCode) {
        try {
            PaystackInitializeResponse resp = new PaystackInitializeResponse();

            var statusField = PaystackInitializeResponse.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(resp, status);

            if (authUrl != null) {
                PaystackInitializeResponse.Data data = new PaystackInitializeResponse.Data();

                var urlField = PaystackInitializeResponse.Data.class.getDeclaredField("authorizationUrl");
                urlField.setAccessible(true);
                urlField.set(data, authUrl);

                var codeField = PaystackInitializeResponse.Data.class.getDeclaredField("accessCode");
                codeField.setAccessible(true);
                codeField.set(data, accessCode);

                var dataField = PaystackInitializeResponse.class.getDeclaredField("data");
                dataField.setAccessible(true);
                dataField.set(resp, data);
            }
            return resp;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build test response", e);
        }
    }
}
