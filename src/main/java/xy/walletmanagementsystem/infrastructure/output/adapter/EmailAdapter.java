package xy.walletmanagementsystem.infrastructure.output.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.output.EmailOutPutPort;
import xy.walletmanagementsystem.domain.model.EmailObject;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailAdapter implements EmailOutPutPort {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendEmail(EmailObject email) {
        if (email == null || email.getRecipient() == null) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email.getRecipient());
            message.setSubject(email.getSubject());
            message.setText(email.getBody());
            javaMailSender.send(message);
            log.info("Email sent to {}", email.getRecipient());
        } catch (Exception e) {
            log.error("Failed to send email to {}", email.getRecipient(), e);
        }
    }
}
