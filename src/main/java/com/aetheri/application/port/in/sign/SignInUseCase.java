package com.aetheri.application.port.in.sign;

import com.aetheri.application.result.kakao.SignInResult;
import reactor.core.publisher.Mono;

/**
 * 사용자 로그인(Sign In)과 관련된 비즈니스 로직을 정의하는 유즈케이스 인터페이스입니다.
 * 이 유즈케이스는 특히 외부 서비스(예: 카카오)의 인증 코드를 사용하여 사용자를 인증하고 토큰을 발급하는 기능을 제공합니다.
 */
public interface SignInUseCase {
    /**
     * 카카오(Kakao)와 같은 OAuth 2.0 제공자로부터 받은 인증 코드({@code code})를 사용하여 로그인 처리를 수행합니다.
     *
     * <p>이 메서드는 다음 단계를 포함합니다:</p>
     * <ol>
     * <li>인증 코드를 사용하여 카카오 API로부터 사용자 액세스 토큰 및 정보를 획득</li>
     * <li>시스템 내부에 사용자 계정이 없다면 회원가입(자동) 처리</li>
     * <li>시스템 내부에서 사용할 액세스 토큰과 리프레시 토큰 발급</li>
     * </ol>
     *
     * @param code 카카오(또는 외부 OAuth 제공자)로부터 받은 인증 코드(Authorization Code) 문자열입니다.
     * @return 로그인 성공 시 발급된 토큰 정보를 담은 {@code SignInResponse} 객체를 발행하는 {@code Mono}입니다.
     */
    Mono<SignInResult> signIn(String code);
}