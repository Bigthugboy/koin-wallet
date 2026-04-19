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
import xy.walletmanagementsystem.domain.messages.UrlConstant;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.infrastructure.input.rest.message.SwaggerUiConstants;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.TransactionResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.mapper.RestMapper;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.stream.Collectors;

import static xy.walletmanagementsystem.domain.messages.ConstantMessages.TRANSACTIONS_RETRIEVED_SUCCESSFULLY;
import static xy.walletmanagementsystem.domain.messages.ConstantMessages.TRANSACTION_RETRIEVED_SUCCESSFULLY;

@RestController
@RequestMapping(UrlConstant.TRANSACTION_URL)
@RequiredArgsConstructor
@Tag(name = SwaggerUiConstants.TRANSACTION_TAG_NAME, description = SwaggerUiConstants.TRANSACTION_TAG_DESCRIPTION)
public class TransactionController {

    private final TransactionOutPutPort transactionOutPutPort;
    private final RestMapper restMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = SwaggerUiConstants.GET_ALL_TRANSACTIONS_SUMMARY, description = SwaggerUiConstants.GET_ALL_TRANSACTIONS_DESCRIPTION)
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        List<Transaction> transactions = transactionOutPutPort.findAll();
        List<TransactionResponse> response = transactions.stream()
                .map(restMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response, TRANSACTIONS_RETRIEVED_SUCCESSFULLY));
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = SwaggerUiConstants.GET_TRANSACTION_SUMMARY, description = SwaggerUiConstants.GET_TRANSACTION_DESCRIPTION)
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(@PathVariable Long transactionId) {
        Transaction transaction = transactionOutPutPort.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(transaction), TRANSACTION_RETRIEVED_SUCCESSFULLY));
    }


}
