package com.nntan041299.englishmasterservice.auth.client;

import com.nntan041299.englishmasterservice.auth.dto.GoogleUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "googleUserInfo", url = "${app.clients.googleUserInfo.url}")
public interface GoogleUserInfoClient {

    @GetMapping("/oauth2/v2/userinfo")
    GoogleUserInfo getUserInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
}
