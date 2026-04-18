package xy.walletmanagementsystem.infrastructure.output.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import xy.walletmanagementsystem.domain.model.EmailObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailAdapterTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailAdapter emailAdapter;

    @Test
    void sendEmail_shouldSendMessageWhenRecipientExists() {
        ReflectionTestUtils.setField(emailAdapter, "fromAddress", "noreply@example.com");
        EmailObject email = EmailObject.builder()
                .recipient("john@example.com")
                .subject("Subject")
                .body("Body")
                .build();

        emailAdapter.sendEmail(email);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());
        SimpleMailMessage sent = messageCaptor.getValue();
        assertEquals("noreply@example.com", sent.getFrom());
        assertEquals("Subject", sent.getSubject());
        assertEquals("Body", sent.getText());
        assertEquals("john@example.com", sent.getTo()[0]);
    }

    @Test
    void sendEmail_shouldIgnoreNullOrMissingRecipient() {
        emailAdapter.sendEmail(null);
        emailAdapter.sendEmail(EmailObject.builder().subject("x").body("y").build());
        verify(javaMailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }
}
