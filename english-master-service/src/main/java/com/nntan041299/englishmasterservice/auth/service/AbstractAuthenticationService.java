package com.nntan041299.englishmasterservice.auth.service;

import com.nntan041299.englishmasterservice.auth.dto.TokenResponse;
import com.nntan041299.englishmasterservice.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Shared base for services that authenticate a user and need to issue JWT
 * access/refresh tokens. Both password-based login ({@code AuthService}) and
 * OAuth2 login ({@code GoogleOauth2Impl}) end with the same step: turn an
 * authenticated {@link UserDetails} into a {@link TokenResponse}.
 */
public abstract class AbstractAuthenticationService {

    protected final JwtUtil jwtUtil;

    protected AbstractAuthenticationService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    protected TokenResponse issueTokens(UserDetails userDetails) {
        return TokenResponse.of(
                jwtUtil.generateAccessToken(userDetails),
                jwtUtil.generateRefreshToken(userDetails)
        );
    }
}
