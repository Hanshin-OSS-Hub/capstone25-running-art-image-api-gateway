package com.aetheri.application.service.sign;

import com.aetheri.application.result.kakao.KakaoIssueTokenResult;
import com.aetheri.application.port.in.sign.SignOffUseCase;
import com.aetheri.application.port.out.kakao.KakaoRefreshTokenPort;
import com.aetheri.application.port.out.kakao.KakaoUnlinkPort;
import com.aetheri.application.port.out.r2dbc.KakaoTokenRepositoryPort;
import com.aetheri.application.port.out.r2dbc.RunnerRepositoryPort;
import com.aetheri.application.port.out.token.RedisRefreshTokenRepositoryPort;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.domain.model.KakaoToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 카카오 계정 연동 해제(Unlink)를 포함하는 **회원 탈퇴(Sign-Off)** 유즈케이스({@link SignOffUseCase})의 구현체입니다.
 * 이 서비스는 카카오 API를 통해 사용자 계정 연결을 해제하고, 시스템 내부에 저장된 모든 사용자 관련 정보(토큰, 사용자 데이터)를 삭제합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignOffService implements SignOffUseCase {
    private final KakaoTokenRepositoryPort kakaoTokenRepositoryPort;
    private final KakaoUnlinkPort kakaoUnlinkPort;
    private final KakaoRefreshTokenPort kakaoRefreshTokenPort;
    private final RunnerRepositoryPort runnerRepositoryPort;
    private final RedisRefreshTokenRepositoryPort redisRefreshTokenRepositoryPort;

    /**
     * JWT 토큰에서 추출된 사용자 ID({@code runnerId})를 사용하여 회원 탈퇴 절차를 진행합니다.
     *
     * <p>회원 탈퇴 절차는 다음과 같습니다:</p>
     * <ol>
     * <li>데이터베이스에서 카카오 토큰 조회 ({@link #findKakaoToken(Long)})</li>
     * <li>카카오 리프레시 토큰 갱신 (안전한 액세스 토큰 확보) ({@link #refreshKakaoToken(KakaoToken)})</li>
     * <li>카카오 API를 통한 사용자 계정 연동 해제(Unlink) ({@link #unlinkKakaoUser(KakaoIssueTokenResult)})</li>
     * <li>데이터베이스의 카카오 토큰 정보 삭제 ({@link #deleteKakaoToken(Long)})</li>
     * <li>Redis의 리프레시 토큰 정보 삭제 ({@link #deleteRefreshTokenFromRedis(String)})</li>
     * <li>데이터베이스의 사용자 정보(Runner) 삭제 ({@link #deleteRunner(Long)})</li>
     * </ol>
     *
     * @param runnerId 회원 탈퇴를 요청하는 사용자의 고유 식별자(ID)입니다.
     * @return 모든 탈퇴 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    @Override
    public Mono<Void> signOff(Long runnerId, String refreshToken) {
        // 데이터베이스에서 사용자 ID로 카카오 토큰을 찾습니다.
        return findKakaoToken(runnerId)
                // 리프레시 토큰을 갱신하여 최신 액세스 토큰 확보
                .flatMap(this::refreshKakaoToken)
                // 카카오에서 회원 탈퇴(연동 해제) 요청
                .flatMap(this::unlinkKakaoUser)
                .onErrorResume(ex -> {
                    log.warn("[SignOffService] 카카오 API 연동 해제 실패. runnerId={}, cause={}", runnerId, ex.toString());
                    return Mono.error(new BusinessException(
                            ErrorMessage.INTERNAL_ERROR_IN_KAKAO_API,
                            "회원 탈퇴 중 카카오 API 에서 오류가 발생했습니다"
                    ));
                })
                // 데이터베이스에 저장된 카카오 토큰 삭제
                .then(deleteKakaoToken(runnerId))
                // Redis에서 시스템 리프레시 토큰 삭제
                .then(deleteRefreshTokenFromRedis(refreshToken))
                // 데이터베이스에 저장된 사용자 정보 삭제
                .then(deleteRunner(runnerId))
                // 성공 시 로그 출력
                .doOnSuccess(v -> log.info("[SignOffService] 사용자 {}가 회원 탈퇴하였습니다.", runnerId));
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
     * @param kakaoToken 재발급에 사용할 리프레시 토큰이 담긴 {@code KakaoToken} 객체입니다.
     * @return 카카오 API에서 받은 새로운 토큰 응답({@code KakaoTokenResponse})을 발행하는 {@code Mono}입니다.
     */
    private Mono<KakaoIssueTokenResult> refreshKakaoToken(KakaoToken kakaoToken) {
        return kakaoRefreshTokenPort
                .refreshAccessToken(kakaoToken.getRefreshToken());
    }

    /**
     * 카카오 API에 연동 해제(Unlink) 요청을 보내 사용자 계정 연결을 해제합니다.
     *
     * @param tokenResponse 연동 해제에 사용할 액세스 토큰이 담긴 DTO입니다.
     * @return 연동 해제 작업이 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    private Mono<Void> unlinkKakaoUser(KakaoIssueTokenResult tokenResponse) {
        return kakaoUnlinkPort.unlink(tokenResponse.accessToken())
                .then();
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
     * 데이터베이스에서 사용자 ID({@code runnerId})에 해당하는 사용자 정보(Runner)를 삭제합니다.
     *
     * @param runnerId 사용자 정보를 삭제할 사용자의 고유 식별자(ID)입니다.
     * @return 삭제 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    private Mono<Void> deleteRunner(Long runnerId) {
        return runnerRepositoryPort.deleteById(runnerId);
    }

    /**
     * Redis 저장소에서 사용자 ID({@code refreshToken})와 연결된 시스템 리프레시 토큰을 삭제합니다.
     *
     * @param refreshToken 리프레시 토큰을 삭제할 사용자의 고유 식별자(ID)입니다.
     * @return 삭제 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    private Mono<Void> deleteRefreshTokenFromRedis(String refreshToken) {
        // Redis 포트는 Boolean을 반환하지만, .then()을 사용하여 Mono<Void>로 변환하여 후속 작업 체이닝을 용이하게 합니다.
        return redisRefreshTokenRepositoryPort.deleteRefreshToken(refreshToken).then();
    }
}