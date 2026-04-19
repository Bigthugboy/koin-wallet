package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.PaystackFundingInitResponse;

import java.math.BigDecimal;

public interface PaymentProviderOutPutPort {
    PaystackFundingInitResponse initializeTransaction(String email, BigDecimal amount) throws WalletManagementException;

    boolean verifyWebhookSignature(String rawPayload, String signature);
}
