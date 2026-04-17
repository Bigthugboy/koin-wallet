package xy.walletmanagementsystem.infrastructure.input.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xy.walletmanagementsystem.applicationPort.input.AuthUseCase;
import xy.walletmanagementsystem.applicationPort.input.OtpUseCase;
import xy.walletmanagementsystem.domain.enums.OtpType;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.AuthResponse;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.LoginRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.PasswordResetRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.SignupRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and authorization")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final OtpUseCase otpUseCase;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) throws WalletManagementException {
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();
        
        AuthResponse response = authUseCase.signup(user, request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) throws WalletManagementException {
        AuthResponse response = authUseCase.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password recovery")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) throws WalletManagementException {
        authUseCase.forgetPassword(email);
        otpUseCase.generateOtp(email, OtpType.PASSWORD_RESET);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to your email"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody PasswordResetRequest request) throws WalletManagementException {
        authUseCase.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password reset successful"));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP")
    public ResponseEntity<ApiResponse<String>> resendOtp(@RequestParam String email, @RequestParam OtpType type) throws WalletManagementException {
        otpUseCase.resendOtp(email, type);
        return ResponseEntity.ok(ApiResponse.ok("OTP resent successfully"));
    }
}
