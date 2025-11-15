package com.aetheri.application.port.in.sign;

import reactor.core.publisher.Mono;

/**
 * 사용자 회원 탈퇴 또는 외부 서비스(예: 카카오)와의 연결 해제(Unlink)와 관련된 비즈니스 로직을 정의하는 유즈케이스 인터페이스입니다.
 * 이 유즈케이스는 사용자 데이터를 삭제하고, 외부 인증 서비스와의 연동을 해제하는 등의 처리를 담당합니다.
 */
public interface SignOffUseCase {
    /**
     * 주어진 사용자 식별자({@code runnerId})에 대해 회원 탈퇴 및 외부 서비스 연동 해제(Unlink) 처리를 수행합니다.
     *
     * <p>이 메서드는 다음을 포함할 수 있습니다:</p>
     * <ol>
     * <li>데이터베이스에서 사용자 관련 모든 데이터 삭제.</li>
     * <li>카카오 API를 호출하여 해당 사용자의 서비스 연결을 해제(Unlink).</li>
     * <li>사용자와 관련된 모든 토큰(예: 리프레시 토큰) 무효화 또는 삭제 처리.</li>
     * </ol>
     *
     * @param runnerId 회원 탈퇴 및 연동 해제 처리를 요청하는 사용자의 고유 식별자(ID)입니다.
     * @param refreshToken 현재 사용자의 리프레시 토큰입니다. 외부 서비스 연동 해제(Unlink) 또는 토큰 무효화에 사용될 수 있습니다.
     * @return 탈퇴 및 모든 후속 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    Mono<Void> signOff(Long runnerId, String refreshToken);
}