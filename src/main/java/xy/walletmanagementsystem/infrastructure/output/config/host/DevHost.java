package xy.walletmanagementsystem.infrastructure.output.config.host;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DevHost implements AllowedHost {
    String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};

    @Value("${allowed.origins:http://localhost:3000,http://127.0.0.1:3000}")
    private String[] patterns;

    @Override
    public String[] getArrayPatterns() {
        return patterns;
    }

    @Override
    public String[] getArrayMethods() {
        return methods;
    }
}
