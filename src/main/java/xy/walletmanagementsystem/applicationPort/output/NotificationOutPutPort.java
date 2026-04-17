package xy.walletmanagementsystem.applicationPort.output;

public interface NotificationOutPutPort {
    void sendLoanNotification(String email, String message);
    void sendPaymentNotification(String email, String message);
}
