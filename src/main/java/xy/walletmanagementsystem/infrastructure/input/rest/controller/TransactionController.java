package xy.walletmanagementsystem.infrastructure.input.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xy.walletmanagementsystem.applicationPort.output.TransactionOutPutPort;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.TransactionResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction History", description = "Endpoints for listing and fetching transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionOutPutPort transactionOutPutPort;

    @GetMapping
    @Operation(summary = "List all transactions (Admin view simulation)")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        List<Transaction> transactions = transactionOutPutPort.findAll();
        List<TransactionResponse> response = transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response, "All transactions retrieved successfully"));
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Fetch a single transaction by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(@PathVariable String transactionId) {
        Transaction transaction = transactionOutPutPort.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(transaction), "Transaction retrieved successfully"));
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .transactionId(t.getTransactionId())
                .userId(t.getUserId())
                .walletId(t.getWalletId())
                .type(t.getType())
                .amount(t.getAmount())
                .status(t.getStatus())
                .referenceNumber(t.getReferenceNumber())
                .timestamp(t.getTimestamp())
                .build();
    }
}
