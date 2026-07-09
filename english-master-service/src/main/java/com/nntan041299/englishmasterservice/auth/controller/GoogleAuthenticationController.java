package com.nntan041299.englishmasterservice.auth.controller;

import com.nntan041299.englishmasterservice.auth.dto.TokenResponse;
import com.nntan041299.englishmasterservice.auth.service.IOauth2Service;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class GoogleAuthenticationController {

    private final IOauth2Service googleOauth2Impl;

    @Autowired
    public GoogleAuthenticationController(@Qualifier("googleOauth2Impl") IOauth2Service googleOauth2Impl) {
        this.googleOauth2Impl = googleOauth2Impl;
    }

    @GetMapping("google/login")
    public ResponseEntity<String> getLoginUrl() {
        String url = googleOauth2Impl.buildAuthUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, url);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("google/getToken")
    public ResponseEntity<TokenResponse> redirectGoogleLogin(
            @RequestParam("state") @NotBlank String state,
            @RequestParam("code") @NotBlank String code) {
        return ResponseEntity.ok(googleOauth2Impl.getAccessToken(code, state));
    }
}
