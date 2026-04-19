package xy.walletmanagementsystem.infrastructure.output.adapter;

import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import xy.walletmanagementsystem.applicationPort.output.TokenBlacklistOutPutPort;
import xy.walletmanagementsystem.infrastructure.output.config.CacheConfig;

@Service
@AllArgsConstructor
public class TokenBlackAdapter implements TokenBlacklistOutPutPort {
    private final CacheManager cacheManager;

    @Override
    public void blacklistToken(String token, long expirationMs) {
        if (token == null) {
            return;
        }
        Cache cache = cacheManager.getCache(CacheConfig.TOKEN_BLACKLIST_CACHE);
        if (cache != null) {
            cache.put(token, true);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        if (token == null) {
            return false;
        }
        Cache cache = cacheManager.getCache(CacheConfig.TOKEN_BLACKLIST_CACHE);
        return cache != null && cache.get(token) != null;
    }
}
