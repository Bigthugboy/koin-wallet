package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xy.walletmanagementsystem.applicationPort.input.AuthUseCase;
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.enums.AccountStatus;
import xy.walletmanagementsystem.domain.enums.UserRole;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.AuthResponse;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;
import xy.walletmanagementsystem.infrastructure.output.config.security.JwtProvider;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserOutPutPort userOutPutPort;
    private final WalletUseCase walletUseCase;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public User signup(User user, String password) throws WalletManagementException {
        if (userOutPutPort.findByEmail(user.getEmail()).isPresent()) {
            throw new WalletManagementException(ErrorMessages.USER_EMAIL_ALREADY_EXISTS);
        }
        User savedUser = buildUserDetails(user, password);
        walletUseCase.createWallet(savedUser.getId());
        return savedUser;
    }

    private User buildUserDetails(User user, String password) {
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(UserRole.USER);
        user.setCreatedDate(LocalDateTime.now());
        user.setUpdatedDate(LocalDateTime.now());
        return userOutPutPort.save(user);
    }

    @Override
    public AuthResponse login(String email, String password) throws WalletManagementException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            String token = jwtProvider.generateAccessToken(authentication);
            
            return AuthResponse.builder()
                    .accessToken(token)
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24)) // Simplified
                    .build();
        } catch (Exception e) {
            log.error("Login failed for email: {}", email, e);
            throw new WalletManagementException("Invalid email or password");
        }
    }

    @Override
    public void forgetPassword(String email) throws WalletManagementException {
        // To be implemented with OtpUseCase
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) throws WalletManagementException {
        // To be implemented with OtpUseCase
    }

    @Override
    public void logout(String userId, String token) throws WalletManagementException {
        // To be implemented with TokenBlacklistOutPutPort
    }

    @Override
    public void changePassword(String userId, String currentPassword, String newPassword, String confirmNewPassword) throws WalletManagementException {
        if (!newPassword.equals(confirmNewPassword)) {
            throw new WalletManagementException("Passwords do not match");
        }
        User user = userOutPutPort.findById(userId)
                .orElseThrow(() -> new WalletManagementException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new WalletManagementException("Invalid current password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedDate(LocalDateTime.now());
        userOutPutPort.save(user);
    }
}
