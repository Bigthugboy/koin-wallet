package xy.walletmanagementsystem.infrastructure.input.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xy.walletmanagementsystem.applicationPort.input.KycUseCase;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.messages.UrlConstant;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.infrastructure.input.rest.message.SwaggerUiConstants;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.KycRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.KycResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.mapper.RestMapper;
import xy.walletmanagementsystem.infrastructure.output.config.security.CustomUserDetails;

@RestController
@RequestMapping(UrlConstant.KYC_URL)
@RequiredArgsConstructor
@Tag(name = SwaggerUiConstants.KYC_TAG_NAME, description = SwaggerUiConstants.KYC_TAG_DESCRIPTION)

public class KycController {

    private final KycUseCase kycUseCase;
    private final RestMapper restMapper;

    @PostMapping("/submit")
    @Operation(summary = SwaggerUiConstants.SUBMIT_KYC_SUMMARY, description = SwaggerUiConstants.SUBMIT_KYC_DESCRIPTION)
    public ResponseEntity<ApiResponse<KycResponse>> submitKyc(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody KycRequest request) throws WalletManagementException {
        Kyc kycDetails = restMapper.toKyc(request, userDetails.getId());
        Kyc kyc = kycUseCase.submitKyc(kycDetails);
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(kyc), "KYC details submitted successfully"));
    }

    @GetMapping("/status")
    @Operation(summary = SwaggerUiConstants.GET_KYC_STATUS_SUMMARY, description = SwaggerUiConstants.GET_KYC_STATUS_DESCRIPTION)
    public ResponseEntity<ApiResponse<KycResponse>> getKycStatus(@AuthenticationPrincipal CustomUserDetails userDetails) throws WalletManagementException {
        Kyc kyc = kycUseCase.getKycDetails(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(kyc), "KYC status retrieved successfully"));
    }
}
