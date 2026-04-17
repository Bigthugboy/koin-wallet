package xy.walletmanagementsystem.domain.enums;

public enum TransactionStatus {
    PENDING(0),
    SUCCESSFUL(1),
    FAILED(2);

    private final int value;

    TransactionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
