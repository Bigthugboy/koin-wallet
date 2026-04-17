package xy.walletmanagementsystem.infrastructure.output.mapper;

import org.mapstruct.Mapper;
import xy.walletmanagementsystem.domain.model.OtpDetails;
import xy.walletmanagementsystem.infrastructure.output.entities.OtpEntity;

@Mapper(componentModel = "spring")
public interface OtpMapper {
    OtpDetails toDomain(OtpEntity entity);
    OtpEntity toEntity(OtpDetails domain);
}
