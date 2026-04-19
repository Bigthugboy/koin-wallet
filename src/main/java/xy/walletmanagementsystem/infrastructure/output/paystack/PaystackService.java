package xy.walletmanagementsystem.infrastructure.output.paystack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xy.walletmanagementsystem.applicationPort.output.PaymentProviderOutPutPort;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.PaystackFundingInitResponse;
import xy.walletmanagementsystem.domain.model.PaystackInitializeRequest;
import xy.walletmanagementsystem.domain.model.PaystackInitializeResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static xy.walletmanagementsystem.domain.messages.EmailRegex.EMAIL_REGEX;
import static xy.walletmanagementsystem.domain.messages.UrlConstant.PAYSTACK_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaystackService implements PaymentProviderOutPutPort {

    private final RestTemplate restTemplate;

    @Value("${paystack.secret.key}")
    private String secretKey;



    @Override
    public PaystackFundingInitResponse initializeTransaction(String email, BigDecimal amount) throws WalletManagementException {
        validatePaystackRequest(email,amount);

        String reference = "KW-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

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
                    PAYSTACK_URL,
                    HttpMethod.POST,
                    entity,
                    PaystackInitializeResponse.class
            );

            PaystackInitializeResponse body = response.getBody();
            if (body == null || !body.isStatus() || body.getData() == null) {
                log.error("Paystack initialization failed: {}", body != null ? body.getMessage() : "null response");
                throw new WalletManagementException(ErrorMessages.TRANSACTION_FAILED);
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
            throw new WalletManagementException(ErrorMessages.ERROR_CONNECTING_TO_PAYMENT_GATEWAY + e.getMessage());
        }
    }

    private void validatePaystackRequest(String email, BigDecimal amount) throws WalletManagementException {
        if (StringUtils.isBlank(email) || !email.matches(EMAIL_REGEX)) {
            throw new WalletManagementException(ErrorMessages.EMAIL_IS_REQUIRED);
        }
        if (amount == null) {
            throw new WalletManagementException(ErrorMessages.AMOUNT_IS_REQUIRED);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletManagementException(ErrorMessages.AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }
    }

@Override
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
