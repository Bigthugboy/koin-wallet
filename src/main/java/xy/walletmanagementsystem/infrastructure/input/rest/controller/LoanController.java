package xy.walletmanagementsystem.infrastructure.input.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xy.walletmanagementsystem.applicationPort.input.LoanUseCase;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.messages.UrlConstant;
import xy.walletmanagementsystem.domain.model.Loan;
import xy.walletmanagementsystem.infrastructure.input.rest.message.SwaggerUiConstants;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.LoanApplicationRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.LoanResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.mapper.RestMapper;
import xy.walletmanagementsystem.infrastructure.output.config.security.CustomUserDetails;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static xy.walletmanagementsystem.domain.messages.UrlConstant.IDEMPOTENCY_KEY;

@RestController
@RequestMapping(UrlConstant.LOAN_URL)
@RequiredArgsConstructor
@Tag(name = SwaggerUiConstants.LOAN_TAG_NAME, description = SwaggerUiConstants.LOAN_TAG_DESCRIPTION)
public class LoanController {

    private final LoanUseCase loanUseCase;
    private final RestMapper restMapper;

    @PostMapping("/apply")
    @Operation(summary = SwaggerUiConstants.APPLY_LOAN_SUMMARY, description = SwaggerUiConstants.APPLY_LOAN_DESCRIPTION)
    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody LoanApplicationRequest request) throws WalletManagementException {
        Loan loan = loanUseCase.applyForLoan(userDetails.getId(), request.getAmount(), request.getDurationInDays());
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(loan), "Loan application submitted successfully"));
    }

    @PostMapping("/{loanId}/approve")
    @Operation(summary = SwaggerUiConstants.APPROVE_LOAN_SUMMARY, description = SwaggerUiConstants.APPROVE_LOAN_DESCRIPTION)
    public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(@PathVariable String loanId) throws WalletManagementException {
        Loan loan = loanUseCase.approveLoan(loanId);
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(loan), "Loan approved successfully"));
    }

    @PostMapping("/{loanId}/disburse")
    @Operation(summary = SwaggerUiConstants.DISBURSE_LOAN_SUMMARY, description = SwaggerUiConstants.DISBURSE_LOAN_DESCRIPTION)
    public ResponseEntity<ApiResponse<LoanResponse>> disburseLoan(@PathVariable String loanId) throws WalletManagementException {
        Loan loan = loanUseCase.disburseLoan(loanId);
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(loan), "Loan disbursed successfully"));
    }

    @PostMapping("/{loanId}/repay")
    @Operation(summary = SwaggerUiConstants.REPAY_LOAN_SUMMARY, description = SwaggerUiConstants.REPAY_LOAN_DESCRIPTION)
    public ResponseEntity<ApiResponse<String>> repayLoan(
            @PathVariable String loanId, 
            @RequestParam BigDecimal amount,
            @RequestHeader(value = IDEMPOTENCY_KEY, required = false) String idempotencyKey
    ) throws WalletManagementException {
        loanUseCase.repayLoan(loanId, amount, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.ok("Repayment processed successfully"));
    }

    @GetMapping("/{loanId}")
    @Operation(summary = SwaggerUiConstants.GET_LOAN_DETAILS_SUMMARY, description = SwaggerUiConstants.GET_LOAN_DETAILS_DESCRIPTION)
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanDetails(@PathVariable String loanId) throws WalletManagementException {
        Loan loan = loanUseCase.getLoanDetails(loanId);
        return ResponseEntity.ok(ApiResponse.success(restMapper.toResponse(loan), "Loan details retrieved successfully"));
    }

    @GetMapping("/my-loans")
    @Operation(summary = SwaggerUiConstants.GET_MY_LOANS_SUMMARY, description = SwaggerUiConstants.GET_MY_LOANS_DESCRIPTION)
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getMyLoans(@AuthenticationPrincipal CustomUserDetails userDetails) throws WalletManagementException {
        List<Loan> loans = loanUseCase.getAllLoansForUser(userDetails.getId());
        List<LoanResponse> response = loans.stream().map(restMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response, "Loans retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = SwaggerUiConstants.GET_ALL_LOANS_SUMMARY, description = SwaggerUiConstants.GET_ALL_LOANS_DESCRIPTION)
    public ResponseEntity<ApiResponse<List<LoanResponse>>> listAllLoans() throws WalletManagementException {
        List<Loan> loans = loanUseCase.listAllLoans();
        List<LoanResponse> response = loans.stream().map(restMapper::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response, "All loans retrieved successfully"));
    }
}
