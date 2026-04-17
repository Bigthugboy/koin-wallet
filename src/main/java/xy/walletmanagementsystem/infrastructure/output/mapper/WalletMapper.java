package xy.walletmanagementsystem.infrastructure.output.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xy.walletmanagementsystem.domain.model.Wallet;
import xy.walletmanagementsystem.infrastructure.output.entities.WalletEntity;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    @Mapping(target = "walletId", source = "id")
    Wallet toDomain(WalletEntity entity);

    @Mapping(target = "id", source = "walletId")
    WalletEntity toEntity(Wallet domain);
}
