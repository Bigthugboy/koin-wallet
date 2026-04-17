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
import xy.walletmanagementsystem.applicationPort.input.WalletUseCase;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.WalletFundingRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.TransactionResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.WalletResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet Management", description = "Endpoints for wallet operations and transaction history")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletUseCase walletUseCase;
    private final UserOutPutPort userOutPutPort;

    @PostMapping("/fund")
    @Operation(summary = "Fund wallet (Simulate payment)")
    public ResponseEntity<ApiResponse<String>> fundWallet(Authentication authentication, @Valid @RequestBody WalletFundingRequest request) throws WalletManagementException {
        String email = authentication.getName();
        User user = userOutPutPort.findByEmail(email)
                .orElseThrow(() -> new WalletManagementException("User not found"));
        
        walletUseCase.fundWallet(user.getId(), request.getAmount(), request.getReference());
        return ResponseEntity.ok(ApiResponse.ok("Wallet funded successfully"));
    }

    @GetMapping("/balance")
    @Operation(summary = "Check wallet balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getBalance(Authentication authentication) throws WalletManagementException {
        String email = authentication.getName();
        User user = userOutPutPort.findByEmail(email)
                .orElseThrow(() -> new WalletManagementException("User not found"));
        
        Wallet wallet = walletUseCase.getWalletBalance(user.getId());
        WalletResponse response = WalletResponse.builder()
                .walletId(wallet.getWalletId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .createdDate(wallet.getCreatedDate())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, "Balance retrieved successfully"));
    }

    @GetMapping("/transactions")
    @Operation(summary = "View transaction history")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(Authentication authentication) throws WalletManagementException {
        String email = authentication.getName();
        User user = userOutPutPort.findByEmail(email)
                .orElseThrow(() -> new WalletManagementException("User not found"));
        
        List<Transaction> transactions = walletUseCase.getTransactionHistory(user.getId());
        List<TransactionResponse> response = transactions.stream()
                .map(t -> TransactionResponse.builder()
                        .transactionId(t.getTransactionId())
                        .userId(t.getUserId())
                        .walletId(t.getWalletId())
                        .type(t.getType())
                        .amount(t.getAmount())
                        .status(t.getStatus())
                        .referenceNumber(t.getReferenceNumber())
                        .timestamp(t.getTimestamp())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction history retrieved successfully"));
    }
}
