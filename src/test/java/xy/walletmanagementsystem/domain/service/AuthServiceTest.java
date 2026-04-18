package xy.walletmanagementsystem.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import xy.walletmanagementsystem.applicationPort.input.OtpUseCase;
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.output.EmailOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.TokenBlacklistOutPutPort;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.enums.AccountStatus;
import xy.walletmanagementsystem.domain.enums.OtpType;
import xy.walletmanagementsystem.domain.enums.UserRole;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.AuthResponse;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.output.config.security.JwtProvider;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserOutPutPort userOutPutPort;
    @Mock
    private WalletUseCase walletUseCase;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenBlacklistOutPutPort tokenBlacklistOutPutPort;
    @Mock
    private OtpUseCase otpUseCase;
    @Mock
    private EmailOutPutPort emailOutPutPort;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user-1")
                .fullName("John Doe")
                .email("john@example.com")
                .phoneNumber("08012345678")
                .build();
    }

    @Test
    void signup_shouldCreateUserAndWalletWithDefaults() throws Exception {
        when(userOutPutPort.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-pass")).thenReturn("encoded-pass");
        when(userOutPutPort.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId("user-1");
            return saved;
        });

        User saved = authService.signup(user, "plain-pass");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userOutPutPort).save(userCaptor.capture());
        verify(walletUseCase).createWallet("user-1");
        User persisted = userCaptor.getValue();
        assertEquals("encoded-pass", persisted.getPasswordHash());
        assertEquals(AccountStatus.ACTIVE, persisted.getStatus());
        assertEquals(UserRole.USER, persisted.getRole());
        assertTrue(persisted.isEmailVerified());
        assertNotNull(saved.getDateCreated());
        verify(emailOutPutPort).sendEmail(any());
    }

    @Test
    void signup_shouldFailWhenEmailExists() {
        when(userOutPutPort.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(WalletManagementException.class, () -> authService.signup(user, "pass"));
        verify(userOutPutPort, never()).save(any(User.class));
    }

    @Test
    void login_shouldReturnJwtTokens() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("john@example.com", "plain-pass");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userOutPutPort.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(user)).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(user)).thenReturn("refresh-token");
        Date expiration = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        when(jwtProvider.getExpirationFromToken("access-token")).thenReturn(expiration);

        AuthResponse response = authService.login("john@example.com", "plain-pass");

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getType());
    }

    @Test
    void forgetPassword_shouldGenerateOtpWhenUserExists() throws Exception {
        when(userOutPutPort.existsByEmail("john@example.com")).thenReturn(true);

        authService.forgetPassword("john@example.com");

        verify(otpUseCase).generateOtp("john@example.com", OtpType.FORGOT_PASSWORD);
    }

    @Test
    void forgetPassword_shouldFailWhenUserNotFound() {
        when(userOutPutPort.existsByEmail("john@example.com")).thenReturn(false);

        assertThrows(WalletManagementException.class, () -> authService.forgetPassword("john@example.com"));
    }

    @Test
    void resetPassword_shouldUpdateEncodedPasswordWhenOtpValid() throws Exception {
        when(otpUseCase.verifyOtp("john@example.com", "123456", OtpType.FORGOT_PASSWORD)).thenReturn(true);
        when(userOutPutPort.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded-new-pass");
        when(userOutPutPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.resetPassword("john@example.com", "123456", "new-pass");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userOutPutPort).save(userCaptor.capture());
        assertEquals("encoded-new-pass", userCaptor.getValue().getPasswordHash());
        verify(emailOutPutPort).sendEmail(any());
    }

    @Test
    void logout_shouldBlacklistTokenWhenValid() throws Exception {
        Date expiration = new Date(System.currentTimeMillis() + 60_000);
        when(userOutPutPort.findById("user-1")).thenReturn(Optional.of(user));
        when(jwtProvider.getExpirationFromToken("token")).thenReturn(expiration);

        authService.logout("user-1", "token");

        verify(tokenBlacklistOutPutPort).blacklistToken(eq("token"), any(Long.class));
    }
}
