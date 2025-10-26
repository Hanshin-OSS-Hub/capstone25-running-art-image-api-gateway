package com.aetheri.application.port.out.kakao;

import com.aetheri.application.result.kakao.KakaoIssueTokenResult;
import reactor.core.publisher.Mono;

/**
 * 카카오(Kakao) OAuth 2.0 서버에 **리프레시 토큰(Refresh Token)**을 사용하여
 * 만료된 토큰을 재발급받기 위한 아웃고잉 포트(Port)입니다.
 * 이 포트는 토큰 갱신 요청을 외부 통신 구현체에 대한 추상화를 제공합니다.
 *
 * @see com.aetheri.infrastructure.adapter.out.kakao.KakaoRefreshTokenAdapter 실제 카카오 API 호출 구현체(어댑터)의 예시입니다.
 */
public interface KakaoRefreshTokenPort {

    /**
     * 카카오 API의 토큰 엔드포인트에 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰의 재발급을 요청합니다.
     *
     * <p>이 요청은 기존의 리프레시 토큰이 유효한 경우에만 성공하며, 성공 시 새로운 액세스 토큰과
     * (갱신된) 리프레시 토큰이 담긴 응답을 반환합니다.</p>
     *
     * @param refreshToken 토큰 재발급(갱신)에 사용할 카카오 리프레시 토큰 문자열입니다.
     * @return 카카오 서버의 응답 데이터를 담은 {@code KakaoTokenResponse} 객체를 발행하는 {@code Mono}입니다.
     */
    Mono<KakaoIssueTokenResult> refreshAccessToken(String refreshToken);
}