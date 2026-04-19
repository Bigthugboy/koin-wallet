package xy.walletmanagementsystem.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.input.WebhookUseCase;
import xy.walletmanagementsystem.applicationPort.output.PaymentProviderOutPutPort;
import xy.walletmanagementsystem.domain.enums.TransactionStatus;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.PaystackWebhookEvent;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

import java.math.BigDecimal;
import java.util.Map;

import static xy.walletmanagementsystem.domain.messages.ConstantMessages.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService implements WebhookUseCase {

    private final WalletUseCase walletUseCase;
    private final PaymentProviderOutPutPort paymentProviderOutPutPort;
    private final ObjectMapper objectMapper;



    @Override
    public void processRawWebhook(String rawBody, String signature) throws WalletManagementException {
        log.info("Received Paystack webhook");

        verifySignature(rawBody, signature);

        PaystackWebhookEvent event = parsePaystackEvent(rawBody);

        routeEvent(event);
    }


    private void verifySignature(String rawBody, String signature) throws WalletManagementException {
        if (signature == null || !paymentProviderOutPutPort.verifyWebhookSignature(rawBody, signature)) {
            log.warn("Invalid webhook signature — request rejected");
            throw new WalletManagementException(ErrorMessages.INVALID_PAYMENT_WEBHOOK);
        }
    }

    PaystackWebhookEvent parsePaystackEvent(String rawBody) throws WalletManagementException {
        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(rawBody, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse webhook payload: {}", e.getMessage());
            throw new WalletManagementException(ErrorMessages.INVALID_WEBHOOK_PAYLOAD_FORMAT);
        }

        String event = requireString(payload.get("event"), "event");

        Map<String, Object> data = requireMap(payload.get("data"), "data");
        String reference  = requireString(data.get("reference"),  "data.reference");
        String rawStatus  = requireString(data.get("status"),     "data.status");
        String rawAmount  = requireString(data.get("amount"),     "data.amount");

        Map<String, Object> customer = requireMap(data.get("customer"), "data.customer");
        String email = requireString(customer.get("email"), "data.customer.email");

        BigDecimal amountInNaira = new BigDecimal(rawAmount).divide(new BigDecimal("100"));

        return PaystackWebhookEvent.builder()
                .event(event)
                .reference(reference)
                .amount(amountInNaira)
                .customerEmail(email)
                .paystackStatus(rawStatus)
                .build();
    }


    private void routeEvent(PaystackWebhookEvent event) throws WalletManagementException {
        log.info("Routing Paystack event '{}' for reference '{}'", event.getEvent(), event.getReference());

        switch (event.getEvent()) {
            case WEBHOOK_CHARGE_SUCCESS -> {
                walletUseCase.confirmFunding(event);
                log.info("charge.success processed — wallet credited. ref={}", event.getReference());
            }
            case WEBHOOK_CHARGE_FAILED -> {
                walletUseCase.markTransactionTerminal(
                        event.getReference(), TransactionStatus.FAILED, event.getEvent());
                log.info("charge.failed processed — transaction marked FAILED. ref={}", event.getReference());
            }
            case WEBHOOK_TRANSFER_SUCCESS -> {
                walletUseCase.markTransactionTerminal(
                        event.getReference(), TransactionStatus.SUCCESSFUL, event.getEvent());
                log.info("transfer.success processed. ref={}", event.getReference());
            }
            case WEBHOOK_TRANSFER_FAILED -> {
                walletUseCase.markTransactionTerminal(
                        event.getReference(), TransactionStatus.FAILED, event.getEvent());
                log.info("transfer.failed processed — transaction marked FAILED. ref={}", event.getReference());
            }
            case WEBHOOK_TRANSFER_REVERSED -> {
                walletUseCase.markTransactionTerminal(
                        event.getReference(), TransactionStatus.REVERSED, event.getEvent());
                log.info("transfer.reversed processed — transaction marked REVERSED. ref={}", event.getReference());
            }
            default -> log.info("Unhandled Paystack event '{}' — ignored.", event.getEvent());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> requireMap(Object value, String field) throws WalletManagementException {
        if (!(value instanceof Map<?, ?> map)) {
            String msg = String.format(ErrorMessages.WEBHOOK_MISSING_FIELD, field);
            log.error(msg);
            throw new WalletManagementException(msg);
        }
        return (Map<String, Object>) map;
    }

    private String requireString(Object value, String field) throws WalletManagementException {
        if (value == null) {
            String msg = String.format(ErrorMessages.WEBHOOK_MISSING_FIELD, field);
            log.error(msg);
            throw new WalletManagementException(msg);
        }
        return value.toString();
    }
}
