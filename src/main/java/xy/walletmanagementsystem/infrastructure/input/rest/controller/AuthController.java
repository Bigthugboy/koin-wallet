package xy.walletmanagementsystem.infrastructure.input.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xy.walletmanagementsystem.applicationPort.input.AuthUseCase;
import xy.walletmanagementsystem.applicationPort.input.OtpUseCase;
import xy.walletmanagementsystem.domain.enums.OtpType;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.messages.UrlConstant;
import xy.walletmanagementsystem.domain.model.AuthResponse;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.input.rest.message.SwaggerUiConstants;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.LoginRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.PasswordResetRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.SignupRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.AuthenticationResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.mapper.RestMapper;
import xy.walletmanagementsystem.infrastructure.output.config.security.CustomUserDetails;

@RestController
@RequestMapping(UrlConstant.AUTH_URL)
@RequiredArgsConstructor
@Tag(name = SwaggerUiConstants.AUTH_TAG_NAME, description = SwaggerUiConstants.AUTH_TAG_DESCRIPTION)
public class AuthController {

    private final AuthUseCase authUseCase;
    private final OtpUseCase otpUseCase;
    private final RestMapper restMapper;

    @PostMapping("/signup")
    @Operation(summary = SwaggerUiConstants.SIGNUP_SUMMARY, description = SwaggerUiConstants.SIGNUP_DESCRIPTION)
    public ResponseEntity<ApiResponse<AuthenticationResponse>> signup(@Valid @RequestBody SignupRequest request) throws WalletManagementException {
        User user = restMapper.toUser(request);
        authUseCase.signup(user, request.getPassword());
        AuthResponse loginResponse = authUseCase.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(loginResponse), "Registration successful"));
    }

    @PostMapping("/login")
    @Operation(summary = SwaggerUiConstants.LOGIN_SUMMARY, description = SwaggerUiConstants.LOGIN_DESCRIPTION)
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@Valid @RequestBody LoginRequest request) throws WalletManagementException {
        AuthResponse response = authUseCase.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(response), "Login successful"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = SwaggerUiConstants.FORGOT_PASSWORD_SUMMARY, description = SwaggerUiConstants.FORGOT_PASSWORD_DESCRIPTION)
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) throws WalletManagementException {
        authUseCase.forgetPassword(email);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to your email"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = SwaggerUiConstants.RESET_PASSWORD_SUMMARY, description = SwaggerUiConstants.RESET_PASSWORD_DESCRIPTION)
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody PasswordResetRequest request) throws WalletManagementException {
        authUseCase.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password reset successful"));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = SwaggerUiConstants.RESEND_OTP_SUMMARY, description = SwaggerUiConstants.RESEND_OTP_DESCRIPTION)
    public ResponseEntity<ApiResponse<String>> resendOtp(@RequestParam String email, @RequestParam OtpType type) throws WalletManagementException {
        otpUseCase.resendOtp(email, type);
        return ResponseEntity.ok(ApiResponse.ok("OTP resent successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = SwaggerUiConstants.LOGOUT_USER, description = SwaggerUiConstants.LOGOUT_USER_DESCRIPTION)
    public ResponseEntity<ApiResponse<String>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) throws WalletManagementException {
        String token = extractToken(authorizationHeader);
        authUseCase.logout(userDetails.getId(), token);
        return ResponseEntity.ok(ApiResponse.ok("Logout successful"));
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
