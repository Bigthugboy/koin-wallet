package xy.walletmanagementsystem.infrastructure.output.mapper;

import org.mapstruct.Mapper;
import xy.walletmanagementsystem.domain.model.Kyc;
import xy.walletmanagementsystem.infrastructure.output.entities.KycEntity;

@Mapper(componentModel = "spring")
public interface KycMapper {
    Kyc toDomain(KycEntity entity);
    KycEntity toEntity(Kyc domain);
}
