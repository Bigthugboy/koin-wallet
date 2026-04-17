package xy.walletmanagementsystem.infrastructure.input.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xy.walletmanagementsystem.applicationPort.input.LoanUseCase;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.Loan;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.LoanApplicationRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.LoanResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loan Management", description = "Endpoints for loan application and processing")
@SecurityRequirement(name = "bearerAuth")
public class LoanController {

    private final LoanUseCase loanUseCase;
    private final UserOutPutPort userOutPutPort;

    @PostMapping("/apply")
    @Operation(summary = "Apply for a loan")
    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(Authentication authentication, @Valid @RequestBody LoanApplicationRequest request) throws WalletManagementException {
        String email = authentication.getName();
        User user = userOutPutPort.findByEmail(email)
                .orElseThrow(() -> new WalletManagementException("User not found"));
        
        Loan loan = loanUseCase.applyForLoan(user.getId(), request.getAmount(), request.getDurationInDays());
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(loan), "Loan application submitted successfully"));
    }

    @PostMapping("/{loanId}/approve")
    @Operation(summary = "Approve a loan (Admin only simulation)")
    public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(@PathVariable String loanId) throws WalletManagementException {
        Loan loan = loanUseCase.approveLoan(loanId);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(loan), "Loan approved successfully"));
    }

    @PostMapping("/{loanId}/disburse")
    @Operation(summary = "Disburse an approved loan")
    public ResponseEntity<ApiResponse<LoanResponse>> disburseLoan(@PathVariable String loanId) throws WalletManagementException {
        Loan loan = loanUseCase.disburseLoan(loanId);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(loan), "Loan disbursed successfully"));
    }

    @PostMapping("/{loanId}/repay")
    @Operation(summary = "Repay a loan")
    public ResponseEntity<ApiResponse<String>> repayLoan(@PathVariable String loanId, @RequestParam BigDecimal amount) throws WalletManagementException {
        loanUseCase.repayLoan(loanId, amount);
        return ResponseEntity.ok(ApiResponse.ok("Repayment processed successfully"));
    }

    @GetMapping("/{loanId}")
    @Operation(summary = "View loan details")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanDetails(@PathVariable String loanId) throws WalletManagementException {
        Loan loan = loanUseCase.getLoanDetails(loanId);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(loan), "Loan details retrieved successfully"));
    }

    @GetMapping("/my-loans")
    @Operation(summary = "List all loans for the current user")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getMyLoans(Authentication authentication) throws WalletManagementException {
        String email = authentication.getName();
        User user = userOutPutPort.findByEmail(email)
                .orElseThrow(() -> new WalletManagementException("User not found"));
        
        List<Loan> loans = loanUseCase.getAllLoansForUser(user.getId());
        List<LoanResponse> response = loans.stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response, "Loans retrieved successfully"));
    }

    private LoanResponse mapToResponse(Loan loan) {
        return LoanResponse.builder()
                .loanId(loan.getLoanId())
                .userId(loan.getUserId())
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .durationInDays(loan.getDurationInDays())
                .status(loan.getStatus())
                .repaymentSchedule(loan.getRepaymentSchedule())
                .createdDate(loan.getCreatedDate())
                .build();
    }
}
