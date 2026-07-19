package com.nntan041299.englishmasterservice.auth.service.impl;

import com.nntan041299.englishmasterservice.auth.dto.CreateUserRequest;
import com.nntan041299.englishmasterservice.auth.dto.LoginRequest;
import com.nntan041299.englishmasterservice.auth.dto.RefreshTokenRequest;
import com.nntan041299.englishmasterservice.auth.dto.TokenResponse;
import com.nntan041299.englishmasterservice.auth.dto.UpdateUserRequest;
import com.nntan041299.englishmasterservice.auth.dto.UserResponse;
import com.nntan041299.englishmasterservice.auth.entity.Role;
import com.nntan041299.englishmasterservice.auth.mapper.UserMapper;
import com.nntan041299.englishmasterservice.auth.service.AbstractAuthenticationService;
import com.nntan041299.englishmasterservice.auth.service.CurrentUserProvider;
import com.nntan041299.englishmasterservice.security.JwtUtil;
import com.nntan041299.englishmasterservice.security.TokenBlacklistService;
import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.auth.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService extends AbstractAuthenticationService {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CurrentUserProvider currentUserProvider;

    public AuthService(UserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil,
                        TokenBlacklistService tokenBlacklistService,
                        UserRepository userRepository,
                        UserMapper userMapper,
                        CurrentUserProvider currentUserProvider) {
        super(jwtUtil);
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.currentUserProvider = currentUserProvider;
    }

    public TokenResponse register(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .languageLevel(request.getLanguageLevel())
                .build();

        User savedUser = userRepository.save(user);
        return issueTokens(savedUser);
    }

    public UserResponse getCurrentUser() {
        return userMapper.toResponse(currentUserProvider.getCurrentUser());
    }

    @Transactional
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        User user = currentUserProvider.getCurrentUser();

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }

        if (request.getLanguageLevel() != null) {
            user.setLanguageLevel(request.getLanguageLevel());
        }

        if (StringUtils.hasText(request.getNewPassword())) {
            if (!StringUtils.hasText(request.getCurrentPassword())) {
                throw new IllegalArgumentException("Current password is required to set a new password");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BadCredentialsException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    public void logout(String bearerToken) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        String token = bearerToken.substring(7);
        if (!jwtUtil.isAccessToken(token)) {
            throw new IllegalArgumentException("Only access tokens can be used to logout");
        }
        Instant expiresAt = jwtUtil.extractExpiration(token).toInstant();
        tokenBlacklistService.revoke(token, expiresAt);
    }

    public TokenResponse login(LoginRequest request) {
        UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());

        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return issueTokens(user);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        String username = jwtUtil.extractUsername(request.getRefreshToken());
        UserDetails user = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.isTokenValid(request.getRefreshToken(), user) || jwtUtil.isAccessToken(request.getRefreshToken())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        return issueTokens(user);
    }
}
