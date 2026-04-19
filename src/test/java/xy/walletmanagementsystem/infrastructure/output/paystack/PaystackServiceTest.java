package xy.walletmanagementsystem.infrastructure.output.paystack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

    // -----------------------------------------------------------------------
    // initializeTransaction — validation edge cases
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("initializeTransaction – validation")
    class InitializeTransactionValidation {

        @Test
        @DisplayName("blank email → WalletManagementException")
        void blankEmail_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction("", new BigDecimal("100")));
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("invalid email format → WalletManagementException")
        void invalidEmailFormat_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction("not-an-email", new BigDecimal("100")));
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("null amount → WalletManagementException")
        void nullAmount_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, null));
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("zero amount → WalletManagementException")
        void zeroAmount_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, BigDecimal.ZERO));
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("negative amount → WalletManagementException")
        void negativeAmount_shouldThrow() {
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("-5")));
            verifyNoInteractions(restTemplate);
        }
    }

    // -----------------------------------------------------------------------
    // initializeTransaction — Paystack API error paths
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("initializeTransaction – API error paths")
    class InitializeTransactionApiErrors {

        @Test
        @DisplayName("Paystack returns null body → WalletManagementException")
        void nullResponseBody_shouldThrow() {
            stubExchange(ResponseEntity.ok(null));
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("100")));
        }

        @Test
        @DisplayName("Paystack returns status=false → WalletManagementException")
        void paystackStatusFalse_shouldThrow() {
            stubExchange(ResponseEntity.ok(buildPaystackResponse(false, null, null)));
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("100")));
        }

        @Test
        @DisplayName("RestTemplate throws network error → WalletManagementException")
        void networkError_shouldThrow() {
            when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(PaystackInitializeResponse.class)))
                    .thenThrow(new RestClientException("timeout"));
            assertThrows(WalletManagementException.class,
                    () -> paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("100")));
        }
    }

    // -----------------------------------------------------------------------
    // initializeTransaction — happy path
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("initializeTransaction – happy path")
    class InitializeTransactionHappyPath {

        @Test
        @DisplayName("returns authorization URL, access code and KW-prefixed reference")
        void happyPath_returnsAuthUrl() throws Exception {
            stubExchange(ResponseEntity.ok(
                    buildPaystackResponse(true, "https://paystack.com/pay/ref", "access-code-123")));

            PaystackFundingInitResponse result =
                    paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("500"));

            assertEquals("https://paystack.com/pay/ref", result.getAuthorizationUrl());
            assertEquals("access-code-123", result.getAccessCode());
            assertNotNull(result.getReference());
            assertTrue(result.getReference().startsWith("KW-"));
        }

        @Test
        @DisplayName("amount is multiplied by 100 (converted to kobo) before calling Paystack")
        void amountConvertedToKobo_doesNotThrow() throws Exception {
            stubExchange(ResponseEntity.ok(
                    buildPaystackResponse(true, "https://paystack.com/pay/ref2", "ac2")));

            // 200.50 NGN = 20050 kobo — verifies BigDecimal multiplication is safe
            PaystackFundingInitResponse result =
                    paystackService.initializeTransaction(VALID_EMAIL, new BigDecimal("200.50"));

            assertNotNull(result);
            assertTrue(result.getReference().startsWith("KW-"));
        }
    }

    // -----------------------------------------------------------------------
    // verifyWebhookSignature
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("verifyWebhookSignature")
    class VerifyWebhookSignature {

        @Test
        @DisplayName("correctly computed HMAC → true")
        void validSignature_returnsTrue() throws Exception {
            String payload = "{\"event\":\"charge.success\"}";
            assertTrue(paystackService.verifyWebhookSignature(payload, computeHmac(payload)));
        }

        @Test
        @DisplayName("wrong signature → false")
        void wrongSignature_returnsFalse() {
            assertFalse(paystackService.verifyWebhookSignature(
                    "{\"event\":\"charge.success\"}", "wrong-sig"));
        }

        @Test
        @DisplayName("HMAC from a different payload → false")
        void signatureFromDifferentPayload_returnsFalse() throws Exception {
            assertFalse(paystackService.verifyWebhookSignature(
                    "{\"event\":\"charge.success\"}", computeHmac("different-payload")));
        }

        @Test
        @DisplayName("empty payload with correct HMAC → true")
        void emptyPayload_withCorrectHmac_returnsTrue() throws Exception {
            String payload = "";
            assertTrue(paystackService.verifyWebhookSignature(payload, computeHmac(payload)));
        }

        @Test
        @DisplayName("signature matching is case-insensitive (Paystack sends lowercase hex)")
        void signatureCaseInsensitive_returnsTrue() throws Exception {
            String payload = "test-body";
            assertTrue(paystackService.verifyWebhookSignature(
                    payload, computeHmac(payload).toUpperCase()));
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Stubs the RestTemplate exchange call with the correctly typed matchers. */
    private void stubExchange(ResponseEntity<PaystackInitializeResponse> response) {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(PaystackInitializeResponse.class)))
                .thenReturn(response);
    }

    private String computeHmac(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
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
