package xy.walletmanagementsystem.infrastructure.input.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xy.walletmanagementsystem.applicationPort.input.KycUseCase;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.KycRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC Management", description = "Endpoints for user KYC verification")
@SecurityRequirement(name = "bearerAuth")
public class KycController {

    private final KycUseCase kycUseCase;
    private final UserOutPutPort userOutPutPort;

    @PostMapping("/submit")
    @Operation(summary = "Submit KYC details (BVN/NIN)")
    public ResponseEntity<ApiResponse<Kyc>> submitKyc(Authentication authentication, @Valid @RequestBody KycRequest request) throws WalletManagementException {
        String email = authentication.getName();
        User user = userOutPutPort.findByEmail(email)
                .orElseThrow(() -> new WalletManagementException("User not found"));
        
        Kyc kyc = kycUseCase.submitKyc(user.getId(), request.getBvn(), request.getNin());
        return ResponseEntity.ok(ApiResponse.success(kyc, "KYC details submitted successfully"));
    }

    @GetMapping("/status")
    @Operation(summary = "Get KYC verification status")
    public ResponseEntity<ApiResponse<Kyc>> getKycStatus(Authentication authentication) throws WalletManagementException {
        String email = authentication.getName();
        User user = userOutPutPort.findByEmail(email)
                .orElseThrow(() -> new WalletManagementException("User not found"));
        
        Kyc kyc = kycUseCase.getKycDetails(user.getId());
        return ResponseEntity.ok(ApiResponse.success(kyc, "KYC status retrieved successfully"));
    }
}
