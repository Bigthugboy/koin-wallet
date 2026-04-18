package xy.walletmanagementsystem.infrastructure.output.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import xy.walletmanagementsystem.applicationPort.output.EmailOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.NotificationOutPutPort;
import xy.walletmanagementsystem.domain.model.EmailObject;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer implements NotificationOutPutPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EmailOutPutPort emailOutPutPort;

    @Override
    public void sendLoanNotification(String email, String message) {
        log.info("Sending loan notification to {}: {}", email, message);
        emailOutPutPort.sendEmail(EmailObject.builder()
                .recipient(email)
                .subject("Loan Update")
                .body(message)
                .build());
        try {
            kafkaTemplate.send("loan-notifications", Map.of(
                    "email", email,
                    "message", message,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.warn("Kafka loan notification publish failed for {}: {}", email, e.getMessage());
        }
    }

    @Override
    public void sendPaymentNotification(String email, String message) {
        log.info("Sending payment notification to {}: {}", email, message);
        emailOutPutPort.sendEmail(EmailObject.builder()
                .recipient(email)
                .subject("Payment Update")
                .body(message)
                .build());
        try {
            kafkaTemplate.send("payment-notifications", Map.of(
                    "email", email,
                    "message", message,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.warn("Kafka payment notification publish failed for {}: {}", email, e.getMessage());
        }
    }
}
