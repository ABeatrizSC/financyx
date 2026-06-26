package com.github.abeatrizsc.financyx.services;

import com.github.abeatrizsc.financyx.domain.User;
import com.github.abeatrizsc.financyx.dto.AccountDetailsDto;
import com.github.abeatrizsc.financyx.dto.DeleteAccountDto;
import com.github.abeatrizsc.financyx.dto.UpdateAccountDto;
import com.github.abeatrizsc.financyx.exceptions.AuthErrorException;
import com.github.abeatrizsc.financyx.exceptions.UserNotFoundException;
import com.github.abeatrizsc.financyx.repositories.UserRepository;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository repository;
    private PasswordEncoder passwordEncoder;

    public User findUserById(String id) {
        return repository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public Optional<User> findUserByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Transactional
    public void create(User newUser) {
        repository.save(newUser);
    }

    @Transactional
    public void updateAccount(String userId, UpdateAccountDto dto) {
        User user = findUserById(userId);

        if (isProvidedPasswordCorrect(dto.getCurrentPassword(), user.getPassword())) {
            if(areDifferentPasswords(dto.getNewPassword(), user.getPassword())) {
                String newPasswordEncoded = passwordEncoder.encode(dto.getNewPassword());
                user.setPassword(newPasswordEncoded);
            }

            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setEmail(dto.getEmail());

            repository.save(user);
        }
    }

    public Boolean isProvidedPasswordCorrect(String providedPassword, String userPassword) {
        if(passwordEncoder.matches(providedPassword, userPassword)) {
            return true;
        }

        throw new AuthErrorException();
    }

    public Boolean areDifferentPasswords(String newPassword, String userPassword){
        if(newPassword == null || newPassword.isEmpty()) {
            return false;
        }

        return !passwordEncoder.matches(newPassword, userPassword);
    }

    @Transactional
    public void deleteAccount(String id, DeleteAccountDto dto) {
        User user = findUserById(id);

        if(isProvidedPasswordCorrect(dto.getPassword(), user.getPassword())) {
            repository.delete(user);
        } else {
            throw new AuthErrorException();
        }
    }

    public AccountDetailsDto getAccountDetails(String userId) {
        User user = findUserById(userId);

        return new AccountDetailsDto(user.getFirstName(), user.getEmail());
    }
}
