package xy.walletmanagementsystem.applicationPort.output;

public interface TokenBlacklistOutPutPort {
    boolean isTokenBlacklisted(String jwt);
}
