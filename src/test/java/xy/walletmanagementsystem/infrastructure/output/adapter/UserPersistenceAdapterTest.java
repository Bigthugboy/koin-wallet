package xy.walletmanagementsystem.infrastructure.output.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.domain.model.User;
import xy.walletmanagementsystem.infrastructure.output.entities.UserEntity;
import xy.walletmanagementsystem.infrastructure.output.mapper.UserMapper;
import xy.walletmanagementsystem.infrastructure.output.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserPersistenceAdapter adapter;

    @Test
    void save_shouldMapDomainEntityAndBack() {
        User user = User.builder().id("u1").build();
        UserEntity entity = UserEntity.builder().id("u1").build();
        when(userMapper.toEntity(user)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toDomain(entity)).thenReturn(user);

        adapter.save(user);

        verify(userRepository).save(entity);
    }

    @Test
    void findByEmail_shouldMapResult() {
        UserEntity entity = UserEntity.builder().id("u1").email("a@a.com").build();
        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(entity));
        when(userMapper.toDomain(any(UserEntity.class))).thenReturn(User.builder().id("u1").build());

        assertTrue(adapter.findByEmail("a@a.com").isPresent());
    }
}
