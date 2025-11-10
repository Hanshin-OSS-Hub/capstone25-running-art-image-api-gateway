package com.aetheri.infrastructure.adapter.in.web.cookie;

import com.aetheri.application.port.in.cookie.CookieUseCase;
import com.aetheri.infrastructure.config.properties.JWTProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtil implements CookieUseCase {
    private final String REFRESH_TOKEN_COOKIE;
    private final long REFRESH_TOKEN_EXPIRE_TIME;

    public CookieUtil(JWTProperties jwtProperties) {
        REFRESH_TOKEN_COOKIE = jwtProperties.refreshTokenCookie();
        REFRESH_TOKEN_EXPIRE_TIME = jwtProperties.refreshTokenExpirationDays() * Duration.ofDays(1).toMillis();
    }

    public ResponseCookie buildCookie(String refreshToken) {
        return ResponseCookie
                .from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)     // JavaScript 접근 방지
                .secure(true)       // HTTPS에서만 전송
                .path("/")          // 전체 경로에서 유효
                .sameSite("Strict") // CSRF 공격 방지
                .maxAge(REFRESH_TOKEN_EXPIRE_TIME) // 쿠키 만료 시간 설정
                .build();
    }
}