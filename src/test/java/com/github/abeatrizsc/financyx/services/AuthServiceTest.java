package com.github.abeatrizsc.financyx.services;

import com.github.abeatrizsc.financyx.domain.User;
import com.github.abeatrizsc.financyx.dto.LoginRequestDto;
import com.github.abeatrizsc.financyx.dto.LoginResponseDto;
import com.github.abeatrizsc.financyx.dto.RegisterRequestDto;
import com.github.abeatrizsc.financyx.exceptions.AuthErrorException;
import com.github.abeatrizsc.financyx.exceptions.EmailAlreadyInUseException;
import com.github.abeatrizsc.financyx.infra.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should return token when credentials are valid on login")
    void shouldReturnTokenWhenCredentialsAreValidOnLogin() {
        User existingUser = createUserAndReturn();
        LoginRequestDto request = new LoginRequestDto("john.doe@email.com", "1234567");

        when(userService.findUserByEmail("john.doe@email.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("1234567", existingUser.getPassword())).thenReturn(true);
        when(tokenService.generateToken(existingUser)).thenReturn("generatedToken");

        LoginResponseDto response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("generatedToken");
        verify(tokenService, times(1)).generateToken(existingUser);
    }

    @Test
    @DisplayName("Should throw AuthErrorException when user is not found on login")
    void shouldThrowAuthErrorExceptionWhenUserNotFoundOnLogin() {
        LoginRequestDto request = new LoginRequestDto("unknown@email.com", "1234567");

        when(userService.findUserByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThrows(AuthErrorException.class, () -> authService.login(request));

        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw AuthErrorException when password is incorrect on login")
    void shouldThrowAuthErrorExceptionWhenPasswordIsIncorrectOnLogin() {
        User existingUser = createUserAndReturn();
        LoginRequestDto request = new LoginRequestDto("john.doe@email.com", "wrongPass");

        when(userService.findUserByEmail("john.doe@email.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPass", existingUser.getPassword())).thenReturn(false);

        assertThrows(AuthErrorException.class, () -> authService.login(request));

        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should register user successfully when email is not in use")
    void shouldRegisterUserSuccessfullyWhenEmailIsNotInUse() {
        RegisterRequestDto request = new RegisterRequestDto("John", "Doe", "john.doe@email.com", "1234567");

        when(userService.findUserByEmail("john.doe@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("1234567")).thenReturn("encodedPassword");

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).create(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@email.com");
        assertThat(savedUser.getFirstName()).isEqualTo("John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("Should throw EmailAlreadyInUseException when email is already registered")
    void shouldThrowEmailAlreadyInUseExceptionWhenEmailIsAlreadyRegistered() {
        User existingUser = createUserAndReturn();
        RegisterRequestDto request = new RegisterRequestDto("John", "Doe", "john.doe@email.com", "1234567");

        when(userService.findUserByEmail("john.doe@email.com")).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyInUseException.class, () -> authService.register(request));

        verify(userService, never()).create(any());
    }

    // -------------------------
    // getAuthenticatedUserId
    // -------------------------

    @Test
    @DisplayName("Should return user id when token is valid")
    void shouldReturnUserIdWhenTokenIsValid() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(tokenService.recoverToken(request)).thenReturn("validToken");
        when(tokenService.validateToken("validToken")).thenReturn("1");

        String userId = authService.getAuthenticatedUserId(request);

        assertThat(userId).isEqualTo("1");
        verify(tokenService, times(1)).recoverToken(request);
        verify(tokenService, times(1)).validateToken("validToken");
    }

    @Test
    @DisplayName("Should return null when token is missing or invalid")
    void shouldReturnNullWhenTokenIsMissingOrInvalid() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(tokenService.recoverToken(request)).thenReturn(null);
        when(tokenService.validateToken(null)).thenReturn(null);

        String userId = authService.getAuthenticatedUserId(request);

        assertThat(userId).isNull();
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