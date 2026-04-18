package xy.walletmanagementsystem.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.applicationPort.output.UserOutPutPort;
import xy.walletmanagementsystem.domain.exception.WalletManagementException;
import xy.walletmanagementsystem.domain.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserOutPutPort userOutPutPort;

    @InjectMocks
    private UserService userService;

    @Test
    void updateUserProfile_shouldUpdateExistingFields() throws Exception {
        User existing = User.builder().id("user-1").fullName("Old").phoneNumber("0801").build();
        User updates = User.builder().fullName("New Name").phoneNumber("0802").build();
        when(userOutPutPort.findById("user-1")).thenReturn(Optional.of(existing));
        when(userOutPutPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUserProfile("user-1", updates);

        assertEquals("New Name", result.getFullName());
        assertEquals("0802", result.getPhoneNumber());
        verify(userOutPutPort).save(existing);
    }

    @Test
    void updateUserProfile_shouldFailWhenUserNotFound() {
        when(userOutPutPort.findById("user-1")).thenReturn(Optional.empty());
        assertThrows(WalletManagementException.class,
                () -> userService.updateUserProfile("user-1", User.builder().build()));
    }

    @Test
    void getUserDetails_shouldFailForBlankId() {
        assertThrows(WalletManagementException.class, () -> userService.getUserDetails(""));
    }
}
