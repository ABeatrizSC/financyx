package com.github.abeatrizsc.financyx.utils;

import com.github.abeatrizsc.financyx.infra.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthRequestUtils {
    private HttpServletRequest request;
    private TokenService tokenService;

    public String getAuthenticatedUserId() {
        String token = request.getHeader("Authorization");

        return tokenService.validateToken(request);
    }
}
