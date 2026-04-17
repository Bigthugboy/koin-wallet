package xy.walletmanagementsystem.infrastructure.output.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import xy.walletmanagementsystem.applicationPort.output.NotificationOutPutPort;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer implements NotificationOutPutPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendLoanNotification(String email, String message) {
        log.info("Sending loan notification to {}: {}", email, message);
        kafkaTemplate.send("loan-notifications", Map.of(
                "email", email,
                "message", message,
                "timestamp", System.currentTimeMillis()
        ));
    }

    @Override
    public void sendPaymentNotification(String email, String message) {
        log.info("Sending payment notification to {}: {}", email, message);
        kafkaTemplate.send("payment-notifications", Map.of(
                "email", email,
                "message", message,
                "timestamp", System.currentTimeMillis()
        ));
    }
}
