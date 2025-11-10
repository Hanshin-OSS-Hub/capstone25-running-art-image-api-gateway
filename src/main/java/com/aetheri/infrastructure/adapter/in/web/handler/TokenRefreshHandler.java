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
    private final String REFRESH_TOKEN_COOKIE;

    public TokenRefreshHandler(
            RefreshTokenUseCase refreshTokenUseCase,
            CookieUseCase cookieUseCase,
            JWTProperties jwtProperties
    ) {
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.cookieUseCase = cookieUseCase;
        this.ACCESS_TOKEN_HEADER = jwtProperties.accessTokenHeader();
        this.REFRESH_TOKEN_COOKIE = jwtProperties.refreshTokenCookie();
    }

    public Mono<ServerResponse> tokenRefresh(ServerRequest request) {
        String refreshToken = resolveToken(request.exchange().getRequest());

        return refreshTokenUseCase.reissueTokens(refreshToken)
                .flatMap(response ->
                        ServerResponse.ok()
                                .cookie(cookieUseCase.buildCookie(response.refreshTokenIssueResult().refreshToken()))
                                .header(ACCESS_TOKEN_HEADER, response.accessToken())
                                .body(Mono.empty(), Void.class)
                );
    }

    private String resolveToken(ServerHttpRequest request) {
        var cookie = request.getCookies().getFirst(REFRESH_TOKEN_COOKIE);
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            return cookie.getValue();
        }
        return null;
    }
}