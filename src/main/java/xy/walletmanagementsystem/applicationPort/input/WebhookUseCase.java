package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;

import java.util.Map;

public interface WebhookUseCase {
    String handlePaystackWebhook(String signature, Map<String, Object> payload) throws WalletManagementException;
}
