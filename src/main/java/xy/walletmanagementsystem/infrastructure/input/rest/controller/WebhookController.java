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
import xy.walletmanagementsystem.domain.service.WebhookService;
import xy.walletmanagementsystem.infrastructure.input.rest.message.SwaggerUiConstants;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = SwaggerUiConstants.WEBHOOK_TAG_NAME, description = SwaggerUiConstants.WEBHOOK_TAG_DESCRIPTION)
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping(value = "/paystack", consumes = "application/json")
    @Operation(summary = SwaggerUiConstants.PAYSTACK_WEBHOOK_SUMMARY, description = SwaggerUiConstants.PAYSTACK_WEBHOOK_DESCRIPTION)
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestHeader(value = "x-paystack-signature", required = false) String signature,
            @RequestBody String rawPayload) {
        try {
            webhookService.handlePaystackWebhookRaw(rawPayload, signature);
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            log.error("Error processing Paystack webhook: {}", e.getMessage());
            // Always return 200 to Paystack — they retry on non-2xx responses
            return ResponseEntity.ok("Error acknowledged");
        }
    }
}
