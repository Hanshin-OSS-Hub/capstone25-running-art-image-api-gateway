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
 * <p>이 서비스 클래스는 클라이언트가 제공한 리프레시 토큰의 유효성을 Redis에서 검증합니다.
 * 유효성이 확인되면 기존 토큰을 삭제(무효화)한 후, 새로운 액세스 토큰과 리프레시 토큰 쌍을
 * 비동기적으로 발급하여 응답합니다.</p>
 *
 * <p>핵심 로직은 반응형 스트림({@code Mono})을 사용하여 논블로킹 방식으로 처리됩니다.
 * JWT 해독 및 생성과 같은 블로킹(Blocking)이 발생하는 작업은 {@link Schedulers#boundedElastic()}
 * 스케줄러를 통해 별도의 스레드 풀에서 안전하게 격리되어 실행됩니다.</p>
 *
 * @see RedisRefreshTokenRepositoryPort Redis에 저장된 리프레시 토큰에 접근하는 비동기 아웃고잉 포트
 * @see JwtTokenProviderPort 새로운 JWT 토큰 생성을 담당하는 아웃고잉 포트 (블로킹/격리 처리 필요)
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {
    private final RedisRefreshTokenRepositoryPort redisRefreshTokenRepositoryPort;
    private final JwtTokenProviderPort jwtTokenProviderPort;

    /**
     * 메인 토큰 재발급 로직: Redis 검증 후 재발급 스트림을 시작합니다.
     *
     * <p>제공된 리프레시 토큰을 키로 사용하여 Redis에서 해당 토큰 메타데이터를 비동기로 조회합니다.
     * 조회에 성공하면, 토큰 문자열과 메타데이터를 다음 단계인 재발급 작업으로 전달합니다.</p>
     *
     * @param refreshToken 클라이언트가 제출한 리프레시 토큰 문자열입니다.
     * @return 새로운 액세스 및 리프레시 토큰 정보를 담은 {@code TokenResult}를 발행하는 {@code Mono<TokenResult>}입니다.
     * @throws BusinessException Redis에서 해당 토큰에 매핑된 {@code RefreshTokenMetadata}를 찾을 수 없을 때 ({@code switchIfEmpty} 오퍼레이터로 처리됨) 발생합니다.
     */
    @Override
    public Mono<TokenResult> refreshToken(String refreshToken) {
        // Redis에서 리프레시 토큰을 찾습니다.
        return findRefreshTokenFromRedis(refreshToken)
                .flatMap(metadata -> {
                    // 2. 토큰 생성에 필요한 사용자 ID 및 Authentication 객체 준비
                    String userId = metadata.userId();
                    // userId()가 Long으로 파싱될 수 있는지 가정하고, Reactive Stream 내에서 Blocking 없이 실행
                    Authentication authentication = AuthenticationConverter.toAuthentication(Long.parseLong(userId));

                    // 3. 기존 토큰 삭제 (무효화)
                    return redisRefreshTokenRepositoryPort.updateRefreshToken(refreshToken, metadata)
                            // 4. 삭제 완료 후, 새로운 토큰 생성 및 Redis에 저장 로직으로 전환
                            .then(createNewTokensAndSaveToRedis(authentication, userId));
                });
    }

    /**
     * 사용자 ID를 사용하여 Redis에서 리프레시 토큰 메타데이터를 비동기로 조회합니다.
     *
     * <p>조회 결과 {@code Mono}가 비어있다면 (토큰이 Redis에 없는 경우),
     * {@code switchIfEmpty} 오퍼레이터를 사용하여 {@code BusinessException}을 발생시켜
     * 토큰 만료 또는 부재 상황을 상위 스트림에 전달합니다.</p>
     *
     * @param refreshToken Redis에서 토큰 조회에 사용할 리프레시 토큰 문자열입니다.
     * @return Redis에서 조회된 {@code RefreshTokenMetadata}를 발행하는 {@code Mono<RefreshTokenMetadata>}입니다.
     * @throws BusinessException Redis에 저장된 리프레시 토큰을 찾을 수 없을 경우 발생합니다.
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
                        // Redis에 있지만 유효하지 않은 토큰(탈취 감지)이면 에러 발생
                        Mono.error(new BusinessException(
                                ErrorMessage.INVALID_REFRESH_TOKEN,
                                "리프레시 토큰이 이미 무효화(Invalidated)되었습니다.")
                        )
                );
    }

    /**
     * 새로운 액세스 및 리프레시 토큰을 생성하고, 새로운 리프레시 토큰 메타데이터를 Redis에 저장합니다.
     *
     * <p>액세스 토큰과 리프레시 토큰의 생성을 각각 블로킹 격리 스케줄러를 사용하여 비동기로 실행합니다.
     * {@code zipWith} 오퍼레이터로 두 토큰 생성이 완료될 때까지 기다린 후,
     * 새로운 리프레시 토큰 정보를 Redis에 비동기로 저장하고 최종 {@code TokenResult}를 반환합니다.</p>
     *
     * @param authentication 토큰 생성에 사용될 사용자 인증 정보입니다.
     * @param runnerId       토큰을 저장할 사용자의 고유 식별자(ID)입니다.
     * @return 새로운 토큰 쌍을 담은 {@code TokenResult}를 발행하는 {@code Mono<TokenResult>}입니다.
     */
    private Mono<TokenResult> createNewTokensAndSaveToRedis(Authentication authentication, String runnerId) {
        // 1. 액세스 토큰 생성을 비동기/격리 처리
        Mono<String> accessTokenMono = Mono.fromCallable(() -> jwtTokenProviderPort.generateAccessToken(authentication))
                .subscribeOn(Schedulers.boundedElastic());

        // 2. 리프레시 토큰 생성을 비동기/격리 처리
        Mono<RefreshTokenIssueResult> refreshTokenMono = Mono.fromCallable(() -> jwtTokenProviderPort.generateRefreshToken(authentication))
                .subscribeOn(Schedulers.boundedElastic());

        // 3. 두 토큰 생성이 완료될 때까지 기다리고 결과를 결합합니다.
        return accessTokenMono.zipWith(refreshTokenMono)
                // 4. 생성된 리프레시 토큰 정보를 Redis에 비동기로 저장합니다.
                .flatMap(tuple -> {
                    String newAccessToken = tuple.getT1();
                    RefreshTokenIssueResult newRefreshTokenResult = tuple.getT2();
                    String newRefreshToken = newRefreshTokenResult.refreshToken();

                    // Redis Key는 'newRefreshToken' 문자열입니다.
                    return redisRefreshTokenRepositoryPort
                            .saveRefreshToken(newRefreshToken, RefreshTokenMetadata.of(runnerId, newRefreshTokenResult.expire()))
                            // 5. Redis 저장이 완료되면 TokenResult를 반환합니다.
                            .thenReturn(TokenResult.of(newAccessToken, newRefreshTokenResult));
                });
    }
}