package xy.walletmanagementsystem.infrastructure.output.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.infrastructure.output.entities.TransactionEntity;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "transactionId", source = "id")
    Transaction toDomain(TransactionEntity entity);

    @Mapping(target = "id", source = "transactionId")
    TransactionEntity toEntity(Transaction domain);
}
