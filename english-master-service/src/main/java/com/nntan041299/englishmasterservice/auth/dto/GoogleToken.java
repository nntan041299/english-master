package com.nntan041299.englishmasterservice.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleToken {

    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("expires_in")
    public String expiresIn;

    @JsonProperty("refresh_token")
    public String refreshToken;

    @JsonProperty("refresh_token_expires_in")
    public String refreshTokenExpiresIn;
}
