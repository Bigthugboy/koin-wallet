package xy.walletmanagementsystem.infrastructure.output.adapter;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.output.TokenBlacklistOutPutPort;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor

public class TokenBlackAdapter implements TokenBlacklistOutPutPort {
    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Override
    public void blacklistToken(String token, long expirationMs) {
        if (token == null || expirationMs <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "true",
                expirationMs,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        if (token == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
