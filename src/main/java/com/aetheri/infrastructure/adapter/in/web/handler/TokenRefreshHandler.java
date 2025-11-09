package com.aetheri.infrastructure.adapter.in.web.handler;

import com.aetheri.application.port.in.token.RefreshTokenUseCase;
import com.aetheri.infrastructure.config.properties.JWTProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class TokenRefreshHandler {
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final String REFRESH_TOKEN_COOKIE;
    private final String ACCESS_TOKEN_HEADER;
    private final long REFRESH_TOKEN_EXPIRE_TIME;

    public TokenRefreshHandler(
            RefreshTokenUseCase refreshTokenUseCase,
            JWTProperties jwtProperties
    ) {
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.REFRESH_TOKEN_COOKIE = jwtProperties.refreshTokenCookie();
        this.REFRESH_TOKEN_EXPIRE_TIME = jwtProperties.refreshTokenExpirationDays() * Duration.ofDays(1).toMillis();
        this.ACCESS_TOKEN_HEADER = jwtProperties.accessTokenHeader();
    }

    public Mono<ServerResponse> tokenRefresh(ServerRequest request) {
        // 1. 모든 쿠키를 가져옵니다.
        MultiValueMap<String, HttpCookie> cookies = request.cookies();

        // 2. "refresh_token" 이름의 쿠키 리스트를 가져옵니다.
        // 쿠키 이름으로 여러 개의 쿠키가 올 수 있으므로 List<HttpCookie> 형태로 반환됩니다.
        HttpCookie refreshTokenCookie = cookies.getFirst(REFRESH_TOKEN_COOKIE);

        String refreshToken = null;
        if (refreshTokenCookie != null) {
            refreshToken = refreshTokenCookie.getValue();
        }

        return refreshTokenUseCase.reissueTokens(refreshToken)
                .flatMap(response -> {
                    ResponseCookie cookie = ResponseCookie
                            .from(REFRESH_TOKEN_COOKIE, response.refreshTokenIssueResult().refreshToken())
                            .httpOnly(true)     // JavaScript 접근 방지
                            .secure(true)       // HTTPS에서만 전송
                            .path("/")          // 전체 경로에서 유효
                            .sameSite("Strict") // CSRF 공격 방지
                            .maxAge(REFRESH_TOKEN_EXPIRE_TIME) // 쿠키 만료 시간 설정
                            .build();

                    return ServerResponse.ok()
                            .cookie(cookie)
                            .header(ACCESS_TOKEN_HEADER, response.accessToken())
                            .body(Mono.empty(), Void.class);
                });
    }
}