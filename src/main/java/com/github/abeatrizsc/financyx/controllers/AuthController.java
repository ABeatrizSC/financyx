package com.github.abeatrizsc.financyx.controllers;

import com.github.abeatrizsc.financyx.services.AuthService;
import com.github.abeatrizsc.financyx.dto.LoginRequestDto;
import com.github.abeatrizsc.financyx.dto.LoginResponseDto;
import com.github.abeatrizsc.financyx.dto.RegisterRequestDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto body){
        LoginResponseDto loginResponseDto = authService.login(body);

        return ResponseEntity.ok(loginResponseDto);
    }

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(@Valid @RequestBody RegisterRequestDto body) {
        authService.register(body);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/authenticated-user")
    public ResponseEntity<String> getAuthenticatedUserId(HttpServletRequest request) {
        return ResponseEntity.ok(authService.getAuthenticatedUserId(request));
    }
}
