package xy.walletmanagementsystem.infrastructure.output.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xy.walletmanagementsystem.domain.model.Loan;
import xy.walletmanagementsystem.infrastructure.output.entities.LoanEntity;

@Mapper(componentModel = "spring")
public interface LoanMapper {
    @Mapping(target = "loanId", source = "id")
    Loan toDomain(LoanEntity entity);

    @Mapping(target = "id", source = "loanId")
    LoanEntity toEntity(Loan domain);
}
