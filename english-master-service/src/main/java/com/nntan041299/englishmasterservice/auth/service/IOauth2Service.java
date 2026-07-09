package com.nntan041299.englishmasterservice.auth.service;

import com.nntan041299.englishmasterservice.auth.dto.TokenResponse;

public interface IOauth2Service {

    String buildAuthUrl();

    TokenResponse getAccessToken(String code, String state);
}