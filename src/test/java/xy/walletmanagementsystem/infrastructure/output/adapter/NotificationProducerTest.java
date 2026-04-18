package xy.walletmanagementsystem.infrastructure.output.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import xy.walletmanagementsystem.applicationPort.output.EmailOutPutPort;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private EmailOutPutPort emailOutPutPort;

    @InjectMocks
    private NotificationProducer producer;

    @Test
    void sendLoanNotification_shouldPublishToLoanTopic() {
        producer.sendLoanNotification("john@example.com", "Loan approved");

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("loan-notifications"), payloadCaptor.capture());
        Map<?, ?> payload = (Map<?, ?>) payloadCaptor.getValue();
        assertEquals("john@example.com", payload.get("email"));
        assertEquals("Loan approved", payload.get("message"));
        verify(emailOutPutPort).sendEmail(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendPaymentNotification_shouldPublishToPaymentTopic() {
        producer.sendPaymentNotification("john@example.com", "Payment received");

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("payment-notifications"), payloadCaptor.capture());
        Map<?, ?> payload = (Map<?, ?>) payloadCaptor.getValue();
        assertEquals("john@example.com", payload.get("email"));
        assertEquals("Payment received", payload.get("message"));
        verify(emailOutPutPort).sendEmail(org.mockito.ArgumentMatchers.any());
    }
}
