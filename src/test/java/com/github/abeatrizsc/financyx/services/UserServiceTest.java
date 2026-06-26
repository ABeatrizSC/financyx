package com.github.abeatrizsc.financyx.services;

import com.github.abeatrizsc.financyx.domain.User;
import com.github.abeatrizsc.financyx.dto.DeleteAccountDto;
import com.github.abeatrizsc.financyx.dto.UpdateAccountDto;
import com.github.abeatrizsc.financyx.exceptions.AuthErrorException;
import com.github.abeatrizsc.financyx.exceptions.UserNotFoundException;
import com.github.abeatrizsc.financyx.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should return user when id exists")
    void shouldFindUserByIdAndReturn() {
        User existingUser = createUserAndReturn();
        when(userRepository.findById("1")).thenReturn(Optional.of(existingUser));

        User foundUser = userService.findUserById("1");

        assertThat(foundUser).isEqualTo(existingUser);
        verify(userRepository, times(1)).findById("1");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldNotFindUserByIdAndReturnUserNotFoundException() {
        String userId = "1";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.findUserById(userId)
        );

        assertThat(exception.getMessage()).contains("User not found");
        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        User existingUser = createUserAndReturn();
        UpdateAccountDto updatedUser = new UpdateAccountDto("New John", "Doe Updated", "john.doe.updated@email.com", "1234567", "new1234");

        when(userRepository.findById("1")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("1234567", existingUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("new1234")).thenReturn("encodedNewPassword");

        userService.updateAccount(existingUser.getId(), updatedUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).findById("1");
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFirstName()).isEqualTo("New John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe Updated");
        assertThat(savedUser.getEmail()).isEqualTo("john.doe.updated@email.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    @DisplayName("Should throw AuthErrorException when password is incorrect on update")
    void shouldNotUpdateUserWhenIncorrectPasswordAndThrowAuthErrorException() {
        User existingUser = createUserAndReturn();
        UpdateAccountDto updatedUserWithWrongPassword = new UpdateAccountDto("New John", "Doe Updated", "john.doe.updated@email.com", "wrongPass", "new1234");

        when(userRepository.findById("1")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPass", existingUser.getPassword())).thenReturn(false);

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> userService.updateAccount(existingUser.getId(), updatedUserWithWrongPassword)
        );

        assertThat(exception.getMessage()).contains("Invalid credentials");
        verify(userRepository, times(1)).findById("1");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user is not found on update")
    void shouldNotUpdateUserWhenUserNotFound() {
        UpdateAccountDto updatedUser = new UpdateAccountDto("New John", "Doe Updated", "john.doe.updated@email.com", "1234567", "new1234");

        when(userRepository.findById("invalidId")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateAccount("invalidId", updatedUser)
        );

        assertThat(exception.getMessage()).contains("User not found");
        verify(userRepository, times(1)).findById("invalidId");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        User existingUser = createUserAndReturn();
        DeleteAccountDto dto = new DeleteAccountDto("1234567");

        when(userRepository.findById("1")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(dto.getPassword(), existingUser.getPassword())).thenReturn(true);

        userService.deleteAccount(existingUser.getId(), dto);

        verify(userRepository, times(1)).findById(existingUser.getId());
        verify(userRepository, times(1)).delete(existingUser);
    }

    @Test
    @DisplayName("Should throw AuthErrorException when password is incorrect on delete")
    void shouldNotDeleteUserWhenIncorrectPasswordAndThrowAuthErrorException() {
        User existingUser = createUserAndReturn();
        DeleteAccountDto deleteUserWithWrongPassword = new DeleteAccountDto("wrongPass");

        when(userRepository.findById("1")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPass", existingUser.getPassword())).thenReturn(false);

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> userService.deleteAccount(existingUser.getId(), deleteUserWithWrongPassword)
        );

        assertThat(exception.getMessage()).contains("Invalid credentials");
        verify(userRepository, times(1)).findById("1");
        verify(userRepository, never()).delete(any());
    }

    User createUserAndReturn() {
        return User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@email.com")
                .password("1234567")
                .build();
    }
}