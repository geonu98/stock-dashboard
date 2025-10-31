package com.stock.dashboard.backend.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JwtAuthenticationResponse {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiryDuration;


    // 신규 회원 여부를 프론트로 전달하기 위한 필드
    @JsonProperty("isNewUser")
    private boolean isNewUser;


    // 프론트에서 추가정보 입력 시 필요한 userId
    @JsonProperty("id")
    private Long userId;

    public JwtAuthenticationResponse(String accessToken, String refreshToken, Long expiryDuration) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiryDuration = expiryDuration;
        tokenType = "Bearer ";
    }

    public JwtAuthenticationResponse(String accessToken) { // 테스트용 임시 생성
        this.accessToken = accessToken;
        this.tokenType = "Bearer ";
    }


    //  신규 생성자: isNewUser, userId 포함
    public JwtAuthenticationResponse(String accessToken, String refreshToken, Long expiryDuration, boolean isNewUser, Long userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiryDuration = expiryDuration;
        this.tokenType = "Bearer ";
        this.isNewUser = isNewUser;
        this.userId = userId;
    }


    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiryDuration() {
        return expiryDuration;
    }

    public void setExpiryDuration(Long expiryDuration) {
        this.expiryDuration = expiryDuration;
    }

    //isNewUser 필드 접근자
    public boolean isNewUser() {return isNewUser;}
    public void setNewUser(boolean newUser) {isNewUser = newUser;}

    public Long getUserId() {return userId;}

    public void setUserId(Long userId) {this.userId = userId;}

}
