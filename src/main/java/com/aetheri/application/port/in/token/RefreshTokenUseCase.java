package com.aetheri.application.port.in.token;

import com.aetheri.application.result.jwt.TokenResult;
import reactor.core.publisher.Mono;

public interface RefreshTokenUseCase {
    Mono<TokenResult> refreshToken(Long runnerId);
    Mono<TokenResult> reissueTokens(String refreshToken);
}