package com.github.abeatrizsc.financyx.services;

import com.github.abeatrizsc.financyx.domain.User;
import com.github.abeatrizsc.financyx.exceptions.AuthErrorException;
import com.github.abeatrizsc.financyx.infra.security.TokenService;
import com.github.abeatrizsc.financyx.exceptions.EmailAlreadyInUseException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.abeatrizsc.financyx.dto.LoginRequestDto;
import com.github.abeatrizsc.financyx.dto.LoginResponseDto;
import com.github.abeatrizsc.financyx.dto.RegisterRequestDto;


import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginResponseDto login(LoginRequestDto body){
        User user = userService.findUserByEmail(body.getEmail()).orElseThrow(AuthErrorException::new);

        if(!passwordEncoder.matches(body.getPassword(), user.getPassword())) {
            throw new AuthErrorException();
        }

        return new LoginResponseDto(tokenService.generateToken(user));
    }

    public void register(RegisterRequestDto body) {
        Optional<User> user = userService.findUserByEmail(body.getEmail());

        if(user.isEmpty()) {
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.getPassword()));
            newUser.setEmail(body.getEmail());
            newUser.setFirstName(body.getFirstName());
            newUser.setLastName(body.getLastName());
            userService.create(newUser);
        } else {
            throw new EmailAlreadyInUseException();
        }

    }

    public String getAuthenticatedUserId(HttpServletRequest request) {
        var token = tokenService.recoverToken(request);

        return tokenService.validateToken(token);
    }
}
