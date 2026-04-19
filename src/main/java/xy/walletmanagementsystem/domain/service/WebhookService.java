package xy.walletmanagementsystem.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.input.WebhookUseCase;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.infrastructure.output.paystack.PaystackService;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService implements WebhookUseCase {

    private final WalletUseCase walletUseCase;
    private final PaystackService paystackService;
    private final ObjectMapper objectMapper;

    @Override
    public String handlePaystackWebhook(String signature, Map<String, Object> payload) throws WalletManagementException {
        // Legacy path — not used when raw body path is enabled; kept for interface compatibility.
        throw new UnsupportedOperationException("Use handlePaystackWebhookRaw instead");
    }

    /**
     * Process a Paystack webhook.
     * Verifies the HMAC SHA-512 signature, then confirms wallet funding if successful.
     *
     * @param rawBody   The raw JSON string body from Paystack
     * @param signature The x-paystack-signature header
     */
    public void handlePaystackWebhookRaw(String rawBody, String signature) throws WalletManagementException {
        log.info("Received Paystack webhook");

        // 1. Verify the webhook signature
        if (signature == null || !paystackService.verifyWebhookSignature(rawBody, signature)) {
            log.warn("Invalid webhook signature — request rejected");
            throw new WalletManagementException("Invalid webhook signature");
        }

        // 2. Parse the raw payload
        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(rawBody, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse webhook payload", e);
            throw new WalletManagementException("Invalid webhook payload format");
        }

        // 3. Only process 'charge.success' events
        String event = stringValue(payload.get("event"));
        if (!"charge.success".equals(event)) {
            log.info("Ignoring webhook event: {}", event);
            return;
        }

        // 4. Extract required fields from data block
        Map<String, Object> data = getMap(payload.get("data"), "data");
        if (!"success".equals(data.get("status"))) {
            log.info("Charge not successful, ignoring.");
            return;
        }

        Map<String, Object> customer = getMap(data.get("customer"), "customer");
        String email = stringValue(customer.get("email"));
        String reference = stringValue(data.get("reference"));
        BigDecimal amountInKobo = new BigDecimal(stringValue(data.get("amount")));
        BigDecimal amountInNaira = amountInKobo.divide(new BigDecimal("100"));

        // 5. Confirm and credit the wallet
        walletUseCase.confirmFunding(reference, amountInNaira, email);
        log.info("Webhook processed. Wallet funded for {}. Ref: {}", email, reference);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Object value, String field) throws WalletManagementException {
        if (!(value instanceof Map<?, ?> mapValue)) {
            throw new WalletManagementException("Invalid webhook payload: missing " + field);
        }
        return (Map<String, Object>) mapValue;
    }

    private String stringValue(Object value) throws WalletManagementException {
        if (value == null) {
            throw new WalletManagementException("Missing required field in webhook payload");
        }
        return value.toString();
    }
}
