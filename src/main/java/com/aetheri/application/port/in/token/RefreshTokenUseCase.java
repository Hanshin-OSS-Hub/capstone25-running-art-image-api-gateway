package com.aetheri.application.port.in.token;

import com.aetheri.application.result.jwt.TokenResult;
import reactor.core.publisher.Mono;

/**
 * 리프래쉬 토큰을 재발급 해주는 유즈케이스입니다.
 * */
public interface RefreshTokenUseCase {

    /**
     * @param refreshToken 갱신이 요청된 리프레시 토큰입니다.
     * @return 새로운 토큰 정보를 담은 {@code TokenResult}를 발행하는 {@code Mono}입니다.
     * */
    Mono<TokenResult> refreshToken(String refreshToken);
}