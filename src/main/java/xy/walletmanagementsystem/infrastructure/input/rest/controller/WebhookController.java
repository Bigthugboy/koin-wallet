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
import xy.walletmanagementsystem.applicationPort.input.WebhookUseCase;
import xy.walletmanagementsystem.domain.messages.UrlConstant;
import xy.walletmanagementsystem.infrastructure.input.rest.message.SwaggerUiConstants;

import static xy.walletmanagementsystem.domain.messages.ConstantMessages.*;

@Slf4j
@RestController
@RequestMapping(UrlConstant.WEBHOOK_URL)
@RequiredArgsConstructor
@Tag(name = SwaggerUiConstants.WEBHOOK_TAG_NAME, description = SwaggerUiConstants.WEBHOOK_TAG_DESCRIPTION)
public class WebhookController {
    private final WebhookUseCase webhookUseCase;

    @PostMapping(value = "/paystack", consumes = "application/json")
    @Operation(
            summary = SwaggerUiConstants.PAYSTACK_WEBHOOK_SUMMARY,
            description = SwaggerUiConstants.PAYSTACK_WEBHOOK_DESCRIPTION)
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestHeader(value = PAYSTACK_SIGNATURE, required = false) String signature,
            @RequestBody String rawPayload) {
        try {
            webhookUseCase.processRawWebhook(rawPayload, signature);
            return ResponseEntity.ok(WEBHOOK_PROCESSED);
        } catch (Exception e) {
            log.error("Error processing Paystack webhook: {}", e.getMessage());
            return ResponseEntity.ok(WEBHOOK_ERROR_ACKNOWLEDGED);
        }
    }
}
