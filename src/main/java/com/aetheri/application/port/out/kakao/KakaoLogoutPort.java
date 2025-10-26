package com.aetheri.application.port.out.kakao;

import reactor.core.publisher.Mono;

/**
 * 카카오(Kakao) 인증 서버에 **로그아웃** 요청을 보내는 기능을 담당하는 아웃고잉 포트(Port)입니다.
 * 이 포트는 주어진 액세스 토큰을 무효화하여 카카오 세션을 종료하는 외부 통신 구현체에 대한 추상화를 제공합니다.
 *
 * @see com.aetheri.infrastructure.adapter.out.kakao.KakaoLogoutAdapter 실제 카카오 API 호출 구현체(어댑터)의 예시입니다.
 */
public interface KakaoLogoutPort {

    /**
     * 카카오 API에 로그아웃을 요청하여 주어진 액세스 토큰을 무효화(Invalidate)합니다.
     *
     * <p>이 처리가 완료되면 해당 액세스 토큰은 더 이상 유효하지 않게 되며,
     * 사용자 세션 종료에 필요한 카카오 측의 조치가 수행됩니다.</p>
     *
     * @param accessToken 무효화(로그아웃) 처리를 요청할 카카오 액세스 토큰 문자열입니다.
     * @return 카카오 로그아웃 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    Mono<Void> logout(String accessToken);
}