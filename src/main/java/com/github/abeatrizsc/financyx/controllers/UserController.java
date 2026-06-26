package com.github.abeatrizsc.financyx.controllers;

import com.github.abeatrizsc.financyx.domain.User;
import com.github.abeatrizsc.financyx.services.UserService;
import com.github.abeatrizsc.financyx.dto.AccountDetailsDto;
import com.github.abeatrizsc.financyx.dto.DeleteAccountDto;
import com.github.abeatrizsc.financyx.dto.UpdateAccountDto;
import com.github.abeatrizsc.financyx.infra.security.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;
    private TokenService tokenService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(HttpServletRequest request, @PathVariable String id) {
        String requestUser = tokenService.validateToken(tokenService.recoverToken(request));

        if (Objects.equals(id, requestUser)) {
            return ResponseEntity.ok(userService.findUserById(id));
        }

        throw new SecurityException();
    }

    @GetMapping("/details")
    public ResponseEntity<AccountDetailsDto> getUserAccountDetails(HttpServletRequest request) {
        String userId = tokenService.validateToken(tokenService.recoverToken(request));

        return ResponseEntity.ok(userService.getAccountDetails(userId));
    }

    @PutMapping
    public ResponseEntity<String> updateUserAccount(HttpServletRequest request, @Valid @RequestBody UpdateAccountDto updateAccountDto) {
        String userId = tokenService.validateToken(tokenService.recoverToken(request));

        userService.updateAccount(userId, updateAccountDto);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUserAccount(HttpServletRequest request, @Valid @RequestBody DeleteAccountDto dto) {
        String userId = tokenService.validateToken(tokenService.recoverToken(request));

        userService.deleteAccount(userId, dto);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
