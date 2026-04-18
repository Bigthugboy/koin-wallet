package xy.walletmanagementsystem.infrastructure.input.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.messages.UrlConstant;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.input.rest.message.SwaggerUiConstants;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.WalletFundingRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.TransactionResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.WalletResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.mapper.RestMapper;
import xy.walletmanagementsystem.infrastructure.output.config.security.CustomUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(UrlConstant.WALLET_URL)
@RequiredArgsConstructor
@Tag(name = SwaggerUiConstants.WALLET_TAG_NAME, description = SwaggerUiConstants.WALLET_TAG_DESCRIPTION)
public class WalletController {

    private final WalletUseCase walletUseCase;
    private final RestMapper restMapper;


    @PostMapping("/create")
    @Operation(summary = SwaggerUiConstants.CREATE_WALLET_SUMMARY, description = SwaggerUiConstants.CREATE_WALLET_DESCRIPTION)
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@AuthenticationPrincipal CustomUserDetails userDetails) throws WalletManagementException {
        Wallet wallet = walletUseCase.createWallet(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(wallet), "Wallet created successfully"));
    }

    @PostMapping("/fund")
    @Operation(summary = SwaggerUiConstants.FUND_WALLET_SUMMARY, description = SwaggerUiConstants.FUND_WALLET_DESCRIPTION)
    public ResponseEntity<ApiResponse<String>> fundWallet(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody WalletFundingRequest request) throws WalletManagementException {
        walletUseCase.fundWallet(userDetails.getId(), request.getAmount(), request.getReference());
        return ResponseEntity.ok(ApiResponse.ok("Wallet funded successfully"));
    }

    @GetMapping("/balance")
    @Operation(summary = SwaggerUiConstants.GET_BALANCE_SUMMARY, description = SwaggerUiConstants.GET_BALANCE_DESCRIPTION)
    public ResponseEntity<ApiResponse<WalletResponse>> getBalance(@AuthenticationPrincipal CustomUserDetails userDetails) throws WalletManagementException {
        Wallet wallet = walletUseCase.getWalletBalance(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(wallet), "Balance retrieved successfully"));
    }

    @GetMapping("/transactions")
    @Operation(summary = SwaggerUiConstants.GET_TRANSACTIONS_SUMMARY, description = SwaggerUiConstants.GET_TRANSACTIONS_DESCRIPTION)
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(@AuthenticationPrincipal CustomUserDetails userDetails) throws WalletManagementException {
        List<Transaction> transactions = walletUseCase.getTransactionHistory(userDetails.getId());
        List<TransactionResponse> response = transactions.stream()
                .map(restMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction history retrieved successfully"));
    }
}
