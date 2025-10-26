package com.aetheri.application.port.out.kakao;

import reactor.core.publisher.Mono;

/**
 * 카카오(Kakao) API 서버에 사용자 **연결 해제(Unlink)**, 즉 회원 탈퇴를 요청하는 기능을 담당하는 아웃고잉 포트(Port)입니다.
 * 이 포트는 주어진 액세스 토큰을 사용하여 사용자 계정과 카카오 서비스의 연동을 해제하는 외부 통신 구현체에 대한 추상화를 제공합니다.
 *
 * @see com.aetheri.infrastructure.adapter.out.kakao.KakaoUnlinkAdapter 실제 카카오 API 호출 구현체(어댑터)의 예시입니다.
 */
public interface KakaoUnlinkPort {

    /**
     * 카카오 API에 연결 해제(Unlink)를 요청하여 사용자의 서비스 연동을 해제하고, 해당 사용자의 고유 ID를 반환합니다.
     *
     * <p>이 처리가 완료되면 해당 액세스 토큰은 무효화되며, 서비스와 카카오 계정 간의 연결이 끊어집니다.
     * 반환되는 ID는 연결 해제된 카카오 사용자 ID입니다.</p>
     *
     * @param accessToken 연결 해제(Unlink) 처리를 요청할 카카오 액세스 토큰 문자열입니다.
     * @return 연결 해제가 성공적으로 처리된 카카오 사용자 ID({@code Long})를 발행하는 {@code Mono} 객체입니다.
     */
    Mono<Long> unlink(String accessToken);
}