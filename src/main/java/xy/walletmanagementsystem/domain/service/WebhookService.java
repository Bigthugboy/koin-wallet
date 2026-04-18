package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.input.WebhookUseCase;
import xy.walletmanagementsystem.applicationPort.output.NotificationOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.User;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService implements WebhookUseCase {

    private final WalletUseCase walletUseCase;
    private final UserOutPutPort userOutPutPort;
    private final NotificationOutPutPort notificationOutPutPort;

    @Override
    public String handlePaystackWebhook(String signature, Map<String, Object> payload) throws WalletManagementException {
        log.info("Received Paystack webhook payload");

        // Signature verification intentionally deferred until paystack integration phase.
        Map<String, Object> data = getDataPayload(payload);
        if (!"success".equals(data.get("status"))) {
            return "Webhook received";
        }

        Map<String, Object> customer = getMap(data.get("customer"), "customer");
        String email = stringValue(customer.get("email"), "customer.email");
        BigDecimal amountInKobo = new BigDecimal(stringValue(data.get("amount"), "amount"));
        BigDecimal amountInNaira = amountInKobo.divide(new BigDecimal("100"));
        String reference = stringValue(data.get("reference"), "reference");

        User user = userOutPutPort.findByEmail(email)
                .orElseThrow(() -> new WalletManagementException("User not found for email: " + email));

        walletUseCase.fundWallet(user.getId(), amountInNaira, reference);
        notificationOutPutPort.sendPaymentNotification(user.getEmail(),
                "Wallet funding of NGN " + amountInNaira + " was successful. Ref: " + reference);
        log.info("Successfully processed payment for user {}", email);
        return "Webhook processed";
    }

    private Map<String, Object> getDataPayload(Map<String, Object> payload) throws WalletManagementException {
        return getMap(payload.get("data"), "data");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Object value, String field) throws WalletManagementException {
        if (!(value instanceof Map<?, ?> mapValue)) {
            throw new WalletManagementException("Invalid webhook payload: missing " + field);
        }
        return (Map<String, Object>) mapValue;
    }

    private String stringValue(Object value, String field) throws WalletManagementException {
        if (value == null) {
            throw new WalletManagementException("Invalid webhook payload: missing " + field);
        }
        return value.toString();
    }
}
