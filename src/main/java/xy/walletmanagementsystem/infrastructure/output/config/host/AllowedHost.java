package xy.walletmanagementsystem.infrastructure.output.config.host;

public interface AllowedHost {
    String[] getArrayPatterns();

    String[] getArrayMethods();
}
