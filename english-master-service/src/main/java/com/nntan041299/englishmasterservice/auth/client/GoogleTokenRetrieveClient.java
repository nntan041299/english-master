package com.nntan041299.englishmasterservice.auth.client;

import com.nntan041299.englishmasterservice.auth.dto.GoogleToken;
import com.nntan041299.englishmasterservice.auth.dto.GoogleTokenRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "googleOauth2Api", url = "${app.clients.googleOauth2Api.url}")
public interface GoogleTokenRetrieveClient {

    @PostMapping(path = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    GoogleToken exchangeCodeForToken(@RequestBody GoogleTokenRequest googleTokenRequest);
}
