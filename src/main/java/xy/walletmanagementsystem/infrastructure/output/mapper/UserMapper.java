package xy.walletmanagementsystem.infrastructure.output.mapper;

import org.mapstruct.Mapper;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.output.entities.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toDomain(UserEntity entity);
    UserEntity toEntity(User domain);
}
