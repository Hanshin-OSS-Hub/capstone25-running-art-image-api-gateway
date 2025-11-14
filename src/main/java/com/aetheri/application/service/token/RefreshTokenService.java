package com.aetheri.application.service.token;

import com.aetheri.application.result.jwt.RefreshTokenIssueResult;
import com.aetheri.application.result.jwt.TokenResult;
import com.aetheri.application.port.in.token.RefreshTokenUseCase;
import com.aetheri.application.port.out.jwt.JwtTokenProviderPort;
import com.aetheri.application.port.out.jwt.JwtTokenResolverPort;
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
    private final JwtTokenResolverPort jwtTokenResolverPort;

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
                // 찾아온 리프레시 토큰을 사용하여 토큰을 재발급합니다.
                .flatMap(refreshTokenMetadata -> reissueTokens(refreshToken, refreshTokenMetadata));
    }

    /**
     * 토큰 재발급 작업 흐름 관리 메서드입니다.
     *
     * <p>토큰 메타데이터에서 사용자 ID를 추출하고, 이를 기반으로 {@code Authentication} 객체를 생성한 후,
     * 기존 토큰을 삭제하고 새로운 토큰 쌍을 생성 및 저장하는 일련의 비동기 작업을 연결합니다.</p>
     *
     * @param refreshToken 현재 사용 중인 리프레시 토큰 문자열입니다.
     * @param refreshTokenMetadata 유효성이 확인된 리프레시 토큰 메타데이터 객체입니다.
     * @return 새로운 토큰 정보를 담은 {@code TokenResult}를 발행하는 {@code Mono<TokenResult>}입니다.
     */
    public Mono<TokenResult> reissueTokens(String refreshToken, RefreshTokenMetadata refreshTokenMetadata) {
        // 리프레시 토큰에서 사용자 ID를 추출합니다. (블로킹 작업을 반응형으로 감싸 처리)
        return extractRunnerIdReactive(refreshTokenMetadata.getUserId())
                // 사용자 ID를 사용해서 Spring Security의 Authentication 객체를 만듭니다.
                .map(AuthenticationConverter::toAuthentication)
                // 만들어진 Authentication으로 기존 토큰을 삭제하고 새 토큰을 생성합니다.
                .flatMap(auth -> deleteOldTokenAndCreateNew(auth, refreshToken));
    }

    /**
     * JWT 해독을 위한 블로킹(Blocking) 호출 격리 래퍼 메서드입니다.
     *
     * <p>JWT 토큰에서 ID를 추출하는 {@code jwtTokenResolverPort.getIdFromToken} 호출은
     * 라이브러리 내부에서 블로킹 연산을 포함할 수 있습니다. 따라서, {@code Mono.fromCallable}을 사용하고
     * {@link Schedulers#boundedElastic()} 스케줄러를 지정하여 이 작업을
     * 별도의 스레드 풀에서 안전하게 비동기적으로 실행되도록 격리합니다.</p>
     *
     * @param refreshToken ID 추출에 사용될 리프레시 토큰 문자열입니다.
     * @return 토큰에서 추출된 사용자 ID(Runner ID)를 발행하는 {@code Mono<Long>}입니다.
     */
    private Mono<Long> extractRunnerIdReactive(String refreshToken) {
        return Mono.fromCallable(() -> jwtTokenResolverPort.getIdFromToken(refreshToken))
                .subscribeOn(Schedulers.boundedElastic()); // 블로킹 호출을 별도 스레드풀에서
    }

    /**
     * 기존 리프레시 토큰을 Redis에서 삭제하고 새로운 토큰 쌍을 생성하는 메서드입니다.
     *
     * <p>사용자 ID를 기반으로 Redis Repository 포트를 통해 기존의 리프레시 토큰을 비동기적으로 삭제합니다.
     * 삭제 작업 완료를 나타내는 {@code Mono<Void>}에 {@code .then()} 오퍼레이터를 연결하여,
     * 삭제가 끝난 후에 새로운 토큰을 생성 및 저장하는 작업을 순차적으로 실행합니다.</p>
     *
     * @param auth 토큰 재발급에 사용될 사용자 인증 정보({@code Authentication})입니다.
     * @param refreshToken 삭제할 기존 리프레시 토큰 문자열입니다.
     * @return 새로운 토큰 정보를 담은 {@code TokenResult}를 발행하는 {@code Mono<TokenResult>}입니다.
     */
    private Mono<TokenResult> deleteOldTokenAndCreateNew(Authentication auth, String refreshToken) {
        String runnerId = auth.getName();
        return redisRefreshTokenRepositoryPort
                // 기존의 리프레시 토큰을 삭제합니다.
                .deleteRefreshToken(refreshToken)
                // 삭제 완료 후, 새로운 리프레시 토큰을 생성하고 저장합니다.
                .then(Mono.defer(() -> createAndSaveRefreshToken(auth, runnerId)));
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
                                "리프레시 토큰을 찾을 수 없습니다.")
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
     * @param runnerId 토큰을 저장할 사용자의 고유 식별자(ID)입니다.
     * @return 새로운 토큰 쌍을 담은 {@code TokenResult}를 발행하는 {@code Mono<TokenResult>}입니다.
     */
    private Mono<TokenResult> createAndSaveRefreshToken(Authentication authentication, String runnerId) {
        // 1. 액세스 토큰 생성을 비동기/격리 처리
        Mono<String> accessTokenMono = Mono.fromCallable(() -> jwtTokenProviderPort.generateAccessToken(authentication))
                .subscribeOn(Schedulers.boundedElastic());

        // 2. 리프레시 토큰 생성을 비동기/격리 처리
        Mono<RefreshTokenIssueResult> refreshTokenMono = Mono.fromCallable(() -> jwtTokenProviderPort.generateRefreshToken(authentication))
                .subscribeOn(Schedulers.boundedElastic());

        // 3. 두 토큰 생성이 완료될 때까지 기다리고 결과를 결합합니다.
        return accessTokenMono.zipWith(refreshTokenMono)
                // 4. 생성된 리프레시 토큰 정보를 Redis에 비동기로 저장합니다.
                .flatMap(tuple -> redisRefreshTokenRepositoryPort
                        .saveRefreshToken(runnerId, RefreshTokenMetadata.of(runnerId, tuple.getT2().expire()))
                        // 5. Redis 저장이 완료되면 TokenResult를 반환합니다.
                        .thenReturn(TokenResult.of(tuple.getT1(), tuple.getT2())));
    }
}