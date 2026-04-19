package xy.walletmanagementsystem.applicationPort.input;

import xy.walletmanagementsystem.domain.exception.WalletManagementException;


public interface WebhookUseCase {

    void processRawWebhook(String rawBody, String signature) throws WalletManagementException;
}
