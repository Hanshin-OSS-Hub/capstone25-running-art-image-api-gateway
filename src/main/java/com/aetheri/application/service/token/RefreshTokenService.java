package com.aetheri.application.service.token;

import com.aetheri.application.result.jwt.RefreshTokenIssueResult;
import com.aetheri.application.result.jwt.TokenResult;
import com.aetheri.application.port.in.token.RefreshTokenUseCase;
import com.aetheri.application.port.out.jwt.JwtTokenProviderPort;
import com.aetheri.application.port.out.token.RedisRefreshTokenRepositoryPort;
import com.aetheri.application.service.converter.AuthenticationConverter;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.domain.model.RefreshTokenMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 리프레시 토큰을 이용한 액세스 및 리프레시 토큰 재발급 서비스 구현체입니다.
 *
 * <p>이 서비스 클래스는 클라이언트가 제공한 리프레시 토큰의 유효성을 Redis에서 비동기적으로 검증합니다.
 * 유효성 검증 성공 시, 기존 토큰을 무효화(삭제)한 후, 새로운 액세스 토큰과 리프레시 토큰 쌍을
 * 생성하여 응답하는 로직을 반응형 스트림({@code Mono})으로 구현합니다.</p>
 *
 * <p>핵심 로직에서 JWT 해독/생성과 같은 블로킹(Blocking) 작업은 {@link Schedulers#boundedElastic()}
 * 스케줄러를 사용하여 별도의 스레드 풀에서 안전하게 격리(Isolation)되어 실행됩니다. 이는 논블로킹 웹 서버의
 * 성능 저하를 방지하기 위함입니다.</p>
 *
 * @see RedisRefreshTokenRepositoryPort Redis에 저장된 리프레시 토큰에 접근하는 비동기 아웃고잉 포트
 * @see JwtTokenProviderPort 새로운 JWT 토큰 생성을 담당하는 아웃고잉 포트 (블로킹 작업 격리 대상)
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {
    private final RedisRefreshTokenRepositoryPort redisRefreshTokenRepositoryPort;
    private final JwtTokenProviderPort jwtTokenProviderPort;

    /**
     * 메인 토큰 재발급 로직: 클라이언트가 제출한 리프레시 토큰의 유효성을 검증하고 새로운 토큰을 발급합니다.
     *
     * <p>처리 흐름:</p>
     * <ol>
     * <li>Redis에서 토큰 메타데이터를 조회하고 유효성을 검증합니다 ({@code findRefreshTokenFromRedis}).</li>
     * <li>토큰이 유효하면, 사용자 ID를 추출하고 {@code Authentication} 객체를 생성합니다.</li>
     * <li>기존 리프레시 토큰을 Redis에서 무효화(업데이트)하여 재사용을 방지합니다 (탈취 감지 패턴: Rolling Token).</li>
     * <li>새로운 액세스 토큰과 리프레시 토큰을 생성하고 Redis에 저장합니다 ({@code createNewTokensAndSaveToRedis}).</li>
     * <li>최종적으로 새로운 토큰 쌍을 담은 {@code TokenResult}를 반환합니다.</li>
     * </ol>
     *
     * @param refreshToken 클라이언트가 제출한 리프레시 토큰 문자열입니다.
     * @return 새로운 액세스 및 리프레시 토큰 정보를 담은 {@code TokenResult}를 발행하는 {@code Mono<TokenResult>}입니다.
     * @throws BusinessException 토큰이 Redis에 없거나(만료/부재), 이미 무효화된 토큰일 경우 발생합니다.
     */
    @Override
    public Mono<TokenResult> refreshToken(String refreshToken) {
        // 1. Redis에서 리프레시 토큰을 찾고 유효성을 검증합니다.
        return findRefreshTokenFromRedis(refreshToken)
                .flatMap(metadata -> {
                    // 2. 토큰 생성에 필요한 사용자 ID 및 Authentication 객체 준비
                    String userId = metadata.userId();
                    // Blocking 없이 Authentication 객체로 변환 (AuthenticationConverter가 non-blocking이라고 가정)
                    Authentication authentication = AuthenticationConverter.toAuthentication(Long.parseLong(userId));

                    // 3. 기존 토큰 삭제 (무효화): 기존 토큰을 무효화(isValid=false) 상태로 업데이트합니다.
                    return redisRefreshTokenRepositoryPort.updateRefreshToken(refreshToken, metadata)
                            // 4. 삭제 완료 후, 새로운 토큰 생성 및 Redis에 저장 로직으로 전환
                            .then(createNewTokensAndSaveToRedis(authentication, userId));
                });
    }

    /**
     * 사용자 ID를 사용하여 Redis에서 리프레시 토큰 메타데이터를 비동기로 조회하고 유효성을 검증합니다.
     *
     * <p>검증 흐름:</p>
     * <ol>
     * <li>{@code getRefreshToken(refreshToken)}: Redis에서 토큰 조회.</li>
     * <li>{@code switchIfEmpty}: 토큰이 없으면 (만료되었거나 유효하지 않은 요청) {@code NOT_FOUND_REFRESH_TOKEN_IN_REDIS} 에러 발생.</li>
     * <li>{@code filter(RefreshTokenMetadata::isValid)}: 조회된 메타데이터의 유효성 검사.</li>
     * <li>두 번째 {@code switchIfEmpty}: 토큰은 있지만 유효하지 않은 경우 (이미 재발급되었거나 탈취 시도로 인해 무효화된 경우), {@code INVALID_REFRESH_TOKEN} 에러 발생.</li>
     * </ol>
     *
     * @param refreshToken Redis에서 토큰 조회에 사용할 리프레시 토큰 문자열입니다.
     * @return 유효성이 확인된 {@code RefreshTokenMetadata}를 발행하는 {@code Mono<RefreshTokenMetadata>}입니다.
     * @throws BusinessException Redis에 저장된 리프레시 토큰을 찾을 수 없거나 이미 무효화되었을 경우 발생합니다.
     */
    private Mono<RefreshTokenMetadata> findRefreshTokenFromRedis(String refreshToken) {
        return redisRefreshTokenRepositoryPort
                .getRefreshToken(refreshToken)
                .switchIfEmpty(
                        Mono.error(new BusinessException(
                                ErrorMessage.NOT_FOUND_REFRESH_TOKEN_IN_REDIS,
                                "리프레시 토큰을 찾을 수 없거나 만료되었습니다.")
                        )
                )
                .filter(RefreshTokenMetadata::isValid)
                .switchIfEmpty(
                        // Redis에 있지만 유효하지 않은 토큰(탈취 감지: 이미 새 토큰 발급으로 무효화됨)이면 에러 발생
                        Mono.error(new BusinessException(
                                ErrorMessage.INVALID_REFRESH_TOKEN,
                                "리프레시 토큰이 이미 무효화(Invalidated)되었습니다.")
                        )
                );
    }

    /**
     * 새로운 액세스 및 리프레시 토큰을 생성하고, 새로운 리프레시 토큰 메타데이터를 Redis에 저장합니다.
     *
     * <p>JWT 생성은 I/O 또는 CPU 바운드 블로킹 작업이므로, {@code Mono.fromCallable}과
     * {@code subscribeOn(Schedulers.boundedElastic())}을 사용하여 논블로킹 메인 스레드로부터 격리합니다.</p>
     *
     * <p>{@code zipWith} 오퍼레이터는 두 토큰 생성이 모두 완료될 때까지 반응형으로 기다린 후,
     * 결과를 결합하여 Redis 저장 로직으로 이동합니다.</p>
     *
     * @param authentication 토큰 생성에 사용될 사용자 인증 정보입니다.
     * @param runnerId       토큰을 저장할 사용자의 고유 식별자(ID)입니다.
     * @return 새로운 토큰 쌍을 담은 {@code TokenResult}를 발행하는 {@code Mono<TokenResult>}입니다.
     */
    private Mono<TokenResult> createNewTokensAndSaveToRedis(Authentication authentication, String runnerId) {
        // 1. 액세스 토큰 생성을 비동기/격리 처리: Schedulers.boundedElastic()에서 실행
        Mono<String> accessTokenMono = Mono.fromCallable(() -> jwtTokenProviderPort.generateAccessToken(authentication))
                .subscribeOn(Schedulers.boundedElastic());

        // 2. 리프레시 토큰 생성을 비동기/격리 처리: Schedulers.boundedElastic()에서 실행
        Mono<RefreshTokenIssueResult> refreshTokenMono = Mono.fromCallable(() -> jwtTokenProviderPort.generateRefreshToken(authentication))
                .subscribeOn(Schedulers.boundedElastic());

        // 3. 두 토큰 생성이 완료될 때까지 기다리고 결과를 결합합니다.
        return accessTokenMono.zipWith(refreshTokenMono)
                // 4. 생성된 리프레시 토큰 정보를 Redis에 비동기로 저장합니다.
                .flatMap(tuple -> {
                    String newAccessToken = tuple.getT1();
                    RefreshTokenIssueResult newRefreshTokenResult = tuple.getT2();
                    String newRefreshToken = newRefreshTokenResult.refreshToken();

                    // Redis Key는 'newRefreshToken' 문자열이며, Value는 메타데이터입니다.
                    return redisRefreshTokenRepositoryPort
                            .saveRefreshToken(newRefreshToken, RefreshTokenMetadata.of(runnerId, newRefreshTokenResult.expire()))
                            // 5. Redis 저장이 완료되면 최종 TokenResult를 반환합니다.
                            .thenReturn(TokenResult.of(newAccessToken, newRefreshTokenResult));
                });
    }
}