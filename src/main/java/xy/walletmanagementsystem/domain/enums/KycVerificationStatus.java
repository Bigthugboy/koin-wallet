package xy.walletmanagementsystem.domain.enums;

public enum KycVerificationStatus {
    PENDING(0),
    VERIFIED(1),
    REJECTED(2);

    private final int value;

    KycVerificationStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
