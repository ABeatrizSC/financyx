package com.github.abeatrizsc.financyx.infra.security;

import com.github.abeatrizsc.financyx.exceptions.NotFoundException;
import com.github.abeatrizsc.financyx.repositories.UserRepository;
import com.github.abeatrizsc.financyx.domain.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@AllArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var userId = tokenService.validateToken(request);

        if(userId != null){
            User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User"));

            var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}