package xy.walletmanagementsystem.domain.enums;

public enum WalletStatus {
    ACTIVE(1),
    SUSPENDED(2),
    PENDING(0);

    private final int value;

    WalletStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
