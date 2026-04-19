package xy.walletmanagementsystem.infrastructure.input.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xy.walletmanagementsystem.applicationPort.input.UserUseCase;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.messages.UrlConstant;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.UpdateUserRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.message.SwaggerUiConstants;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.UserResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.mapper.RestMapper;
import xy.walletmanagementsystem.infrastructure.output.config.security.CustomUserDetails;

import java.util.Optional;

import static xy.walletmanagementsystem.domain.messages.ConstantMessages.PROFILE_RETRIEVED;
import static xy.walletmanagementsystem.domain.messages.ConstantMessages.PROFILE_UPDATED;

@RestController
@RequestMapping(UrlConstant.USER_URL)
@RequiredArgsConstructor
@Tag(name = SwaggerUiConstants.USER_TAG_NAME, description = SwaggerUiConstants.USER_TAG_DESCRIPTION)
public class UserController {

    private final UserUseCase userUseCase;
    private final RestMapper restMapper;

    @GetMapping("/profile")
    @Operation(summary = SwaggerUiConstants.GET_PROFILE_SUMMARY, description = SwaggerUiConstants.GET_PROFILE_DESCRIPTION)
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) throws WalletManagementException {
        Optional<User> user = userUseCase.getUserDetails(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(user.get()), PROFILE_RETRIEVED));
    }

    @PutMapping("/profile")
    @Operation(summary = SwaggerUiConstants.UPDATE_PROFILE_SUMMARY, description = SwaggerUiConstants.UPDATE_PROFILE_DESCRIPTION)
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UpdateUserRequest updateUserREquest) throws WalletManagementException {
        User profileUpdates = restMapper.toUpdateUser(updateUserREquest);
        User updatedUser = userUseCase.updateUserProfile(userDetails.getId(), profileUpdates);
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(updatedUser), PROFILE_UPDATED));
    }
}
