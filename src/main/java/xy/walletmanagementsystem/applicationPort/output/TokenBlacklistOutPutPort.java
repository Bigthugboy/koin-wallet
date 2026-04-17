package xy.walletmanagementsystem.applicationPort.output;

public interface TokenBlacklistOutPutPort {
    boolean isTokenBlacklisted(String jwt);

    void blacklistToken(String token, long remainingTimeMs);
}
