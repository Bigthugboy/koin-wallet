package xy.walletmanagementsystem.infrastructure.output.paystack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaystackService {

    private final RestTemplate restTemplate;

    @Value("${paystack.secret.key}")
    private String secretKey;

    @Value("${paystack.base-url}")
    private String baseUrl;

    /**
     * Initialize a Paystack transaction. Saves a PENDING transaction locally and returns the
     * Paystack authorization URL to redirect the user to for payment.
     *
     * @param email  Customer email address
     * @param amount Amount in Naira
     * @return PaystackFundingInitResponse containing the authorization URL and reference
     */
    public PaystackFundingInitResponse initializeTransaction(String email, BigDecimal amount)
            throws WalletManagementException {
        String reference = "KW-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        // Paystack requires amount in Kobo (multiply by 100)
        long amountInKobo = amount.multiply(new BigDecimal("100")).longValue();

        PaystackInitializeRequest requestBody = PaystackInitializeRequest.builder()
                .email(email)
                .amount(amountInKobo)
                .reference(reference)
                .build();

        HttpHeaders headers = buildHeaders();
        HttpEntity<PaystackInitializeRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<PaystackInitializeResponse> response = restTemplate.exchange(
                    baseUrl + "/transaction/initialize",
                    HttpMethod.POST,
                    entity,
                    PaystackInitializeResponse.class
            );

            PaystackInitializeResponse body = response.getBody();
            if (body == null || !body.isStatus() || body.getData() == null) {
                log.error("Paystack initialization failed: {}", body != null ? body.getMessage() : "null response");
                throw new WalletManagementException("Failed to initialize payment with Paystack");
            }

            log.info("Paystack transaction initialized. Reference: {}", reference);
            return PaystackFundingInitResponse.builder()
                    .authorizationUrl(body.getData().getAuthorizationUrl())
                    .accessCode(body.getData().getAccessCode())
                    .reference(reference)
                    .build();

        } catch (WalletManagementException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling Paystack API: {}", e.getMessage());
            throw new WalletManagementException("Error connecting to payment gateway: " + e.getMessage());
        }
    }

    /**
     * Verify HMAC SHA-512 signature from Paystack webhook to ensure authenticity.
     *
     * @param rawPayload The raw JSON body string from the webhook request
     * @param signature  The x-paystack-signature header value
     * @return true if the signature is valid
     */
    public boolean verifyWebhookSignature(String rawPayload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String computed = hexString.toString();
            log.debug("Computed signature: {}, Received signature: {}", computed, signature);
            return computed.equalsIgnoreCase(signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to verify webhook signature: {}", e.getMessage());
            return false;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + secretKey);
        return headers;
    }
}
