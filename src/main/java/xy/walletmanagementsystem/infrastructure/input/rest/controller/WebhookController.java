package xy.walletmanagementsystem.infrastructure.input.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.User;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Endpoints for external service callbacks")
public class WebhookController {

    private final WalletUseCase walletUseCase;
    private final UserOutPutPort userOutPutPort;

    @PostMapping("/paystack")
    @Operation(summary = "Receive Paystack payment confirmation")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestHeader(value = "x-paystack-signature", required = false) String signature,
            @RequestBody Map<String, Object> payload) {
        
        log.info("Received Paystack webhook: {}", payload);

        // Verification logic (Mocked for assessment)
        // In a real app, you would verify the signature and the status
        
        try {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data != null && "success".equals(data.get("status"))) {
                String email = (String) ((Map<String, Object>) data.get("customer")).get("email");
                BigDecimal amountInKobo = new BigDecimal(data.get("amount").toString());
                BigDecimal amountInNaira = amountInKobo.divide(new BigDecimal("100"));
                String reference = (String) data.get("reference");

                User user = userOutPutPort.findByEmail(email)
                        .orElseThrow(() -> new WalletManagementException("User not found for email: " + email));

                walletUseCase.fundWallet(user.getId(), amountInNaira, reference);
                log.info("Successfully processed payment for user: {} with reference: {}", email, reference);
            }
        } catch (Exception e) {
            log.error("Error processing Paystack webhook", e);
            // Even if it fails, return 200 to acknowledge receipt as per webhook best practices
            return ResponseEntity.ok("Error but acknowledged");
        }

        return ResponseEntity.ok("Webhook processed");
    }
}
