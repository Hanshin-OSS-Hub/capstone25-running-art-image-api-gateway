package com.aetheri.application.port.out.kakao;

import com.aetheri.application.result.kakao.KakaoIssueTokenResult;
import reactor.core.publisher.Mono;

/**
 * 카카오(Kakao) OAuth 2.0 서버로부터 **액세스 토큰**을 발급받기 위한 아웃고잉 포트(Port)입니다.
 * 이 포트는 인증 코드(Authorization Code)를 사용하여 토큰 엔드포인트에 요청을 보내는 외부 통신 구현체에 대한 추상화를 제공합니다.
 *
 * @see com.aetheri.infrastructure.adapter.out.kakao.KakaoGetAccessTokenAdapter 실제 카카오 API 호출 구현체(어댑터)의 예시입니다.
 */
public interface KakaoGetAccessTokenPort {

    /**
     * 카카오 인증 서버의 토큰 엔드포인트(Token Endpoint)에 액세스 토큰 발급을 요청합니다.
     *
     * <p>이 요청은 인증 코드를 포함하며, 성공 시 액세스 토큰과 리프레시 토큰이 담긴 응답을 반환합니다.</p>
     *
     * @param code 카카오 로그인 성공 후 리다이렉션 시 받은 **인증 코드(Authorization Code)** 문자열입니다.
     * @return 카카오 서버의 응답 데이터를 담은 {@code KakaoIssueTokenResult} 객체를 발행하는 {@code Mono}입니다.
     */
    Mono<KakaoIssueTokenResult> tokenRequest(String code);
}