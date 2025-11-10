package com.aetheri.infrastructure.adapter.in.web.handler;

import com.aetheri.application.port.in.cookie.CookieUseCase;
import com.aetheri.application.port.in.token.RefreshTokenUseCase;
import com.aetheri.infrastructure.config.properties.JWTProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class TokenRefreshHandler {
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final CookieUseCase cookieUseCase;
    private final String ACCESS_TOKEN_HEADER;

    public TokenRefreshHandler(
            RefreshTokenUseCase refreshTokenUseCase,
            CookieUseCase cookieUseCase,
            JWTProperties jwtProperties
    ) {
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.cookieUseCase = cookieUseCase;
        this.ACCESS_TOKEN_HEADER = jwtProperties.accessTokenHeader();
    }

    public Mono<ServerResponse> tokenRefresh(ServerRequest request) {
        String refreshToken = resolveToken((ServerHttpRequest) request);

        return refreshTokenUseCase.reissueTokens(refreshToken)
                .flatMap(response ->
                        ServerResponse.ok()
                                .cookie(cookieUseCase.buildCookie(response.refreshTokenIssueResult().refreshToken()))
                                .header(ACCESS_TOKEN_HEADER, response.accessToken())
                                .body(Mono.empty(), Void.class)
                );
    }

    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(ACCESS_TOKEN_HEADER);
        // JWTProperties에 설정된 액세스 토큰 헤더 이름 사용
        if (StringUtils.hasText(bearerToken) && StringUtils.startsWithIgnoreCase(bearerToken, "Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }
}