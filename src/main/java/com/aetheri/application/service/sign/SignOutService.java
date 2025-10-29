package com.aetheri.application.service.sign;

import com.aetheri.application.result.kakao.KakaoIssueTokenResult;
import com.aetheri.application.port.in.sign.SignOutUseCase;
import com.aetheri.application.port.out.kakao.KakaoLogoutPort;
import com.aetheri.application.port.out.kakao.KakaoRefreshTokenPort;
import com.aetheri.application.port.out.r2dbc.KakaoTokenRepositoryPort;
import com.aetheri.application.port.out.token.RedisRefreshTokenRepositoryPort;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.domain.model.KakaoToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 사용자 로그아웃(Sign-Out) 유즈케이스({@link SignOutUseCase})를 구현하는 서비스 클래스입니다.
 * 이 서비스는 카카오 API를 통해 사용자 계정의 카카오 세션을 종료하고, 시스템 내부에 저장된
 * 사용자의 모든 토큰 정보(카카오 토큰, 시스템 리프레시 토큰)를 삭제하여 로그아웃 상태를 완성합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignOutService implements SignOutUseCase {
    private final KakaoTokenRepositoryPort kakaoTokenRepositoryPort;
    private final KakaoRefreshTokenPort kakaoRefreshTokenPort;
    private final KakaoLogoutPort kakaoLogoutPort;
    private final RedisRefreshTokenRepositoryPort redisRefreshTokenRepositoryPort;

    /**
     * 사용자 ID({@code runnerId})를 사용하여 로그아웃 절차를 진행합니다.
     *
     * <p>이 절차는 카카오 세션 종료와 시스템 토큰 정리로 구성됩니다.
     * **핵심 구현 원칙:** 카카오 API 요청 중 오류가 발생하더라도, 시스템 내부의 토큰 삭제는 계속 진행하여
     * 서버 측 로그아웃은 성공하도록 보장합니다.</p>
     *
     * @param runnerId 로그아웃을 요청한 사용자의 고유 식별자(ID)입니다.
     * @return 모든 로그아웃 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    @Override
    public Mono<Void> signOut(Long runnerId) {
        // 데이터베이스에서 카카오 토큰을 조회합니다.
        return findKakaoToken(runnerId)
                // 리프레시 토큰을 재발급 받아 최신 액세스 토큰을 확보합니다.
                .flatMap(this::refreshKakaoToken)
                // 카카오 로그아웃 API에 요청을 보내 카카오 세션을 종료합니다.
                .flatMap(this::kakaoLogout)
                // 만약 카카오 로그아웃 요청이 실패하더라도, 시스템 로그아웃은 계속 진행합니다.
                .onErrorResume(ex -> {
                    log.warn("[SignOutService] 카카오 API 로그아웃 실패. runnerId={}, cause={}", runnerId, ex.toString());
                    return Mono.empty(); // 로컬 정리 계속을 위해 빈 Mono를 반환
                })
                // 데이터베이스의 카카오 토큰을 삭제합니다.
                .then(deleteKakaoToken(runnerId))
                // Redis의 시스템 리프레시 토큰을 삭제합니다.
                .then(deleteRefreshToken(runnerId))
                // 성공했다면 성공을 로깅합니다.
                .doOnSuccess(v -> log.info("[SignOutService] 로그아웃 성공: {}", runnerId));
    }

    /**
     * 데이터베이스에서 사용자 ID({@code runnerId})를 사용해 저장된 카카오 토큰 엔티티를 조회합니다.
     *
     * @param runnerId 카카오 토큰을 조회할 사용자의 고유 식별자(ID)입니다.
     * @return 조회된 {@code KakaoToken} 엔티티를 발행하는 {@code Mono}입니다.
     * @throws BusinessException 해당 사용자 ID로 카카오 토큰을 찾을 수 없을 경우 {@code NOT_FOUND_KAKAO_TOKEN} 예외를 발생시킵니다.
     */
    private Mono<KakaoToken> findKakaoToken(Long runnerId) {
        return kakaoTokenRepositoryPort.findByRunnerId(runnerId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        ErrorMessage.NOT_FOUND_KAKAO_TOKEN,
                        "카카오 토큰을 찾을 수 없습니다."))
                );
    }

    /**
     * 카카오 리프레시 토큰을 사용하여 새로운 액세스 토큰 쌍을 재발급받습니다.
     *
     * @param kakaoToken 재발급에 사용할 리프레시 토큰이 담긴 객체입니다.
     * @return 카카오 API에서 받은 새로운 토큰 응답({@code KakaoTokenResponse})을 발행하는 {@code Mono}입니다.
     */
    private Mono<KakaoIssueTokenResult> refreshKakaoToken(KakaoToken kakaoToken) {
        return kakaoRefreshTokenPort
                .refreshAccessToken(kakaoToken.getRefreshToken());
    }

    /**
     * 카카오 API에 로그아웃 요청을 보내 해당 액세스 토큰을 무효화합니다.
     *
     * @param kakaoIssueTokenResult 재발급받은 액세스 토큰이 담긴 DTO입니다.
     * @return 카카오 로그아웃 작업이 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    private Mono<Void> kakaoLogout(KakaoIssueTokenResult kakaoIssueTokenResult) {
        return kakaoLogoutPort.logout(kakaoIssueTokenResult.accessToken());
    }

    /**
     * 데이터베이스에서 사용자 ID({@code runnerId})와 연결된 카카오 토큰 정보를 삭제합니다.
     *
     * @param runnerId 카카오 토큰 정보를 삭제할 사용자의 고유 식별자(ID)입니다.
     * @return 삭제 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    private Mono<Void> deleteKakaoToken(Long runnerId) {
        return kakaoTokenRepositoryPort.deleteByRunnerId(runnerId);
    }

    /**
     * Redis 저장소에서 사용자 ID({@code runnerId})와 연결된 시스템 리프레시 토큰을 삭제합니다.
     *
     * @param runnerId 리프레시 토큰을 삭제할 사용자의 고유 식별자(ID)입니다.
     * @return 삭제 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    private Mono<Void> deleteRefreshToken(Long runnerId) {
        return redisRefreshTokenRepositoryPort.deleteRefreshToken(runnerId).then();
    }
}