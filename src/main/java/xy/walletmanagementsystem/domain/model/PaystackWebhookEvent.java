package xy.walletmanagementsystem.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Immutable value object representing a parsed Paystack webhook event.
 * Created once from the raw JSON body and passed down through the domain layer,
 * eliminating raw {@code Map<String,Object>} threading.
 */
@Getter
@Builder
public class PaystackWebhookEvent {

    /** Paystack event type, e.g. "charge.success", "transfer.reversed". */
    private final String event;

    /** Paystack transaction reference. */
    private final String reference;

    /**
     * Transaction amount converted to major currency units (Naira).
     * Paystack sends amounts in kobo; conversion happens during parsing.
     */
    private final BigDecimal amount;

    /** Customer email extracted from the {@code data.customer} block. */
    private final String customerEmail;

    /**
     * Raw {@code data.status} string from the Paystack payload
     * ("success", "failed", "reversed", etc.).
     */
    private final String paystackStatus;
}
