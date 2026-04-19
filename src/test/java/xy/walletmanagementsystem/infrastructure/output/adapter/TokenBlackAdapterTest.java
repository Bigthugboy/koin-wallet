package xy.walletmanagementsystem.infrastructure.output.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import xy.walletmanagementsystem.infrastructure.output.config.CacheConfig;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlackAdapterTest {

    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;

    private TokenBlackAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TokenBlackAdapter(cacheManager);
    }

    @Test
    void blacklistToken_shouldStoreTokenInCache() {
        when(cacheManager.getCache(CacheConfig.TOKEN_BLACKLIST_CACHE)).thenReturn(cache);
        String token = "valid-token";
        adapter.blacklistToken(token, 10_000);
        verify(cache).put(token, true);
    }

    @Test
    void blacklistToken_shouldIgnoreNullToken() {
        adapter.blacklistToken(null, 10_000);
        verify(cache, never()).put(anyString(), any());
    }

    @Test
    void isTokenBlacklisted_shouldReturnCacheStatus() {
        when(cacheManager.getCache(CacheConfig.TOKEN_BLACKLIST_CACHE)).thenReturn(cache);
        String token = "blacklisted-token";
        when(cache.get(token)).thenReturn(mock(Cache.ValueWrapper.class));
        assertTrue(adapter.isTokenBlacklisted(token));

        when(cache.get("active-token")).thenReturn(null);
        assertFalse(adapter.isTokenBlacklisted("active-token"));
    }
}
