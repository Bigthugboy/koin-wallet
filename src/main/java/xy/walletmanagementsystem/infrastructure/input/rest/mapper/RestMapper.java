package xy.walletmanagementsystem.infrastructure.input.rest.mapper;

import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xy.walletmanagementsystem.domain.model.AuthResponse;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.domain.model.Loan;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.KycRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.SignupRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.request.UpdateUserRequest;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.AuthenticationResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.KycResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.LoanResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.TransactionResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.UserResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.WalletResponse;

@Mapper(componentModel = "spring")
public interface RestMapper {

    UserResponse toResponse(User user);

    WalletResponse toResponse(Wallet wallet);

    LoanResponse toResponse(Loan loan);

    TransactionResponse toResponse(Transaction transaction);

    KycResponse toResponse(Kyc kyc);

    AuthenticationResponse toResponse(AuthResponse authResponse);

    User toUser(@Valid SignupRequest request);

    User toUpdateUser(UpdateUserRequest updateUserRequest);

    Kyc toKyc(@Valid KycRequest request,Long userId);
}
