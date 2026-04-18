package xy.walletmanagementsystem.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xy.walletmanagementsystem.applicationPort.input.AuthUseCase;
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
import xy.walletmanagementsystem.domain.model.EmailObject;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;
import xy.walletmanagementsystem.infrastructure.output.config.security.JwtProvider;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserOutPutPort userOutPutPort;
    private final WalletUseCase walletUseCase;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistOutPutPort tokenBlacklistOutPutPort;
    private final OtpUseCase otpUseCase;
    private final EmailOutPutPort emailOutPutPort;

    @Override
    @Transactional
    public User signup(User user, String password) throws WalletManagementException {
        if (userOutPutPort.findByEmail(user.getEmail()).isPresent()) {
            throw new WalletManagementException(ErrorMessages.USER_EMAIL_ALREADY_EXISTS);
        }
        User savedUser = buildUserDetails(user, password);
        walletUseCase.createWallet(savedUser.getId());
        sendWelcomeEmail(savedUser);
        return savedUser;
    }

    private User buildUserDetails(User user, String password) {
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRole(UserRole.USER);
        user.setEmailVerified(true);
        user.setCreatedDate(LocalDateTime.now());
        user.setUpdatedDate(LocalDateTime.now());
        return userOutPutPort.save(user);
    }

    @Override
    public AuthResponse login(String email, String password) throws WalletManagementException {
        validateEmailAndPassword(email, password);
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Optional<User> user = userOutPutPort.findByEmail(email);
        if (user.isEmpty()) {
            throw new WalletManagementException(ErrorMessages.USER_NOT_FOUND);
        }
        String accessToken = jwtProvider.generateAccessToken(user.get());
        String refreshToken = jwtProvider.generateRefreshToken(user.get());
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = jwtProvider.getExpirationFromToken(accessToken)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    public void forgetPassword(String email) throws WalletManagementException {
        validateEmailFormat(email);
        if (userOutPutPort.existsByEmail(email)) {
            otpUseCase.generateOtp(email, OtpType.FORGOT_PASSWORD);
            log.info("Forget password OTP triggered for {}", email);
        } else {
            throw new WalletManagementException(ErrorMessages.USER_NOT_FOUND);
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) throws WalletManagementException {
        validateEmailFormat(email);
        if (StringUtils.isBlank(otp)) {
            throw new WalletManagementException(ErrorMessages.OTP_IS_REQUIRED);
        }
        if (StringUtils.isBlank(newPassword)) {
            throw new WalletManagementException(ErrorMessages.NEW_PASSWORD_IS_REQUIRED);
        }
        boolean isVerified = otpUseCase.verifyOtp(email, otp, OtpType.FORGOT_PASSWORD);
        if (isVerified) {
            Optional<User> user = userOutPutPort.findByEmail(email);
            if (user.isEmpty()){
                throw new WalletManagementException(ErrorMessages.USER_NOT_FOUND);
            }
            user.get().setPasswordHash(passwordEncoder.encode(newPassword));
            userOutPutPort.save(user.get());
            sendPasswordResetSuccessEmail(email);
            log.info("Password reset successful for user {}", email);
        } else {
            throw new WalletManagementException(ErrorMessages.OTP_INVALID);
        }
    }

    @Override
    public void logout(String userId, String token) throws WalletManagementException {
        if (StringUtils.isBlank(userId)) {
            throw new WalletManagementException(ErrorMessages.USER_ID_IS_REQUIRED);
        }
        Optional<User> user = userOutPutPort.findById(userId);
        if (user.isEmpty()){
            throw new WalletManagementException(ErrorMessages.USER_NOT_FOUND);
        }
        if (token != null) {
            try {
                Date expiration = jwtProvider.getExpirationFromToken(token);
                long remainingTimeMs = expiration.getTime() - System.currentTimeMillis();
                if (remainingTimeMs > 0) {
                    tokenBlacklistOutPutPort.blacklistToken(token, remainingTimeMs);
                }
            } catch (Exception e) {
                log.warn("Could not blacklist token during logout for user {}: {}", userId, e.getMessage());
            }
        }
        log.info("User {} logged out successfully", userId);
    }


    private void validateEmailAndPassword(String email, String password) throws WalletManagementException {
        validateEmailFormat(email);
        if(StringUtils.isBlank(password)) {
            throw new WalletManagementException(ErrorMessages.INVALID_CREDENTIALS);
        }
    }

    private void validateEmailFormat(String email) throws WalletManagementException {
        if (StringUtils.isBlank(email)) {
            throw new WalletManagementException(ErrorMessages.EMAIL_IS_REQUIRED);
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            throw new WalletManagementException(ErrorMessages.INVALID_EMAIL);
        }
    }

    private void sendWelcomeEmail(User user) {
        emailOutPutPort.sendEmail(EmailObject.builder()
                .recipient(user.getEmail())
                .subject("Welcome to Koin Wallet")
                .body("Hi " + user.getFullName() + ", your account has been created successfully.")
                .build());
    }

    private void sendPasswordResetSuccessEmail(String email) {
        emailOutPutPort.sendEmail(EmailObject.builder()
                .recipient(email)
                .subject("Password Reset Successful")
                .body("Your password has been changed successfully.")
                .build());
    }
}
