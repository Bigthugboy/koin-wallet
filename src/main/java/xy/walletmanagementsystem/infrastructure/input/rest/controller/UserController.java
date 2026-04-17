package xy.walletmanagementsystem.infrastructure.input.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xy.walletmanagementsystem.applicationPort.input.UserUseCase;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.UserResponse;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing user profiles")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserUseCase userUseCase;
    private final UserOutPutPort userOutPutPort;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userOutPutPort.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .role(user.getRole())
                .createdDate(user.getCreatedDate())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, "Profile retrieved successfully"));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(Authentication authentication, @RequestBody User profileUpdate) {
        String email = authentication.getName();
        User user = userOutPutPort.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User updatedUser = userUseCase.updateUserProfile(user.getId(), profileUpdate);
        
        UserResponse response = UserResponse.builder()
                .id(updatedUser.getId())
                .fullName(updatedUser.getFullName())
                .email(updatedUser.getEmail())
                .phoneNumber(updatedUser.getPhoneNumber())
                .status(updatedUser.getStatus())
                .role(updatedUser.getRole())
                .createdDate(updatedUser.getCreatedDate())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }
}
