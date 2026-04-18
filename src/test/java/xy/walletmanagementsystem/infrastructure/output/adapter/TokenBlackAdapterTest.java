package xy.walletmanagementsystem.infrastructure.output.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlackAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void blacklistToken_shouldStoreTokenWithExpiry() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        TokenBlackAdapter adapter = new TokenBlackAdapter(redisTemplate);

        adapter.blacklistToken("token", 10_000);

        verify(valueOperations).set(eq("jwt:blacklist:token"), eq("true"), eq(10_000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void blacklistToken_shouldIgnoreInvalidInput() {
        TokenBlackAdapter adapter = new TokenBlackAdapter(redisTemplate);
        adapter.blacklistToken(null, 0);
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void isTokenBlacklisted_shouldReturnRedisLookupResult() {
        TokenBlackAdapter adapter = new TokenBlackAdapter(redisTemplate);
        when(redisTemplate.hasKey("jwt:blacklist:token")).thenReturn(true);
        assertTrue(adapter.isTokenBlacklisted("token"));

        when(redisTemplate.hasKey("jwt:blacklist:missing")).thenReturn(false);
        assertFalse(adapter.isTokenBlacklisted("missing"));
    }
}
