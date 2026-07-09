package com.nntan041299.englishmasterservice.auth.service.impl;

import com.nntan041299.englishmasterservice.auth.client.GoogleTokenRetrieveClient;
import com.nntan041299.englishmasterservice.auth.client.GoogleUserInfoClient;
import com.nntan041299.englishmasterservice.auth.dto.GoogleToken;
import com.nntan041299.englishmasterservice.auth.dto.GoogleTokenRequest;
import com.nntan041299.englishmasterservice.auth.dto.GoogleUserInfo;
import com.nntan041299.englishmasterservice.auth.dto.TokenResponse;
import com.nntan041299.englishmasterservice.auth.entity.Role;
import com.nntan041299.englishmasterservice.auth.entity.User;
import com.nntan041299.englishmasterservice.auth.repository.UserRepository;
import com.nntan041299.englishmasterservice.auth.service.AbstractAuthenticationService;
import com.nntan041299.englishmasterservice.auth.service.IOauth2Service;
import com.nntan041299.englishmasterservice.auth.service.OAuthStateStore;
import com.nntan041299.englishmasterservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
@Qualifier("googleOauth2Impl")
public class GoogleOauth2Impl extends AbstractAuthenticationService implements IOauth2Service {

    private static final String RESPONSE_TYPE = "code";
    private static final String INCLUDE_GRANTED_SCOPES = "true";
    private static final String GRANT_TYPE = "authorization_code";

    @Value("${spring.security.oauth2.client.registration.google.url}")
    private String url;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.scope}")
    private String scope;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUrl;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenRetrieveClient googleTokenRetrieveClient;
    private final GoogleUserInfoClient googleUserInfoClient;
    private final OAuthStateStore oAuthStateStore;

    public GoogleOauth2Impl(JwtUtil jwtUtil,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             GoogleTokenRetrieveClient googleTokenRetrieveClient,
                             GoogleUserInfoClient googleUserInfoClient,
                             OAuthStateStore oAuthStateStore) {
        super(jwtUtil);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.googleTokenRetrieveClient = googleTokenRetrieveClient;
        this.googleUserInfoClient = googleUserInfoClient;
        this.oAuthStateStore = oAuthStateStore;
    }

    @Override
    public String buildAuthUrl() {
        String state = UUID.randomUUID().toString();
        oAuthStateStore.save(state);

        return UriComponentsBuilder.fromUriString(url)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUrl)
                .queryParam("response_type", RESPONSE_TYPE)
                .queryParam("scope", scope)
                .queryParam("include_granted_scopes", INCLUDE_GRANTED_SCOPES)
                .queryParam("access_type", "offline")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Override
    public TokenResponse getAccessToken(String code, String state) {
        if (!oAuthStateStore.validateAndConsume(state)) {
            throw new IllegalArgumentException("Invalid or expired OAuth state");
        }

        GoogleTokenRequest tokenRequest = GoogleTokenRequest.builder()
                .code(code)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUrl)
                .grantType(GRANT_TYPE)
                .build();

        GoogleToken googleToken = googleTokenRetrieveClient.exchangeCodeForToken(tokenRequest);
        GoogleUserInfo userInfo = googleUserInfoClient.getUserInfo("Bearer " + googleToken.getAccessToken());

        User user = findOrCreateUser(userInfo);
        return issueTokens(user);
    }

    private User findOrCreateUser(GoogleUserInfo userInfo) {
        return userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username(generateUniqueUsername(userInfo.getEmail()))
                                .email(userInfo.getEmail())
                                .fullName(buildFullName(userInfo))
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .role(Role.USER)
                                .isActive(true)
                                .build()));
    }

    private String generateUniqueUsername(String email) {
        String base = email.substring(0, email.indexOf('@'));
        String candidate = base;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return candidate;
    }

    private String buildFullName(GoogleUserInfo userInfo) {
        String fullName = (nullToEmpty(userInfo.getFirstName()) + " " + nullToEmpty(userInfo.getLastName())).trim();
        return StringUtils.hasText(fullName) ? fullName : userInfo.getEmail();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
