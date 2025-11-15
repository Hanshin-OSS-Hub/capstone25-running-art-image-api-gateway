package com.aetheri.application.port.in.sign;

import reactor.core.publisher.Mono;

/**
 * 사용자 로그아웃(Sign-Out)과 관련된 비즈니스 로직을 정의하는 유즈케이스 인터페이스입니다.
 * 이 유즈케이스는 카카오(Kakao)와 같은 외부 서비스의 토큰을 무효화하고
 * 사용자의 세션을 안전하게 종료하는 기능을 담당합니다.
 */
public interface SignOutUseCase {
    /**
     * 주어진 사용자 식별자({@code runnerId})에 대해 로그아웃 처리를 수행하고, 외부 서비스 토큰을 무효화합니다.
     *
     * <p>이 메서드는 다음을 포함할 수 있습니다:</p>
     * <ol>
     * <li>카카오 API를 호출하여 해당 사용자의 액세스 토큰 및 리프레시 토큰을 무효화(Invalidate)합니다.</li>
     * <li>시스템 내부에 저장된 해당 사용자의 세션 정보나 리프레시 토큰을 제거합니다.</li>
     * </ol>
     *
     * @param runnerId 로그아웃 처리를 요청하는 사용자의 고유 식별자(ID)입니다.
     * @return 로그아웃 및 토큰 무효화 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    Mono<Void> signOut(Long runnerId, String refreshToken);
}