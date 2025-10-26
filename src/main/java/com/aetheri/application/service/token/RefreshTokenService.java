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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 리프레시 토큰(Refresh Token)을 사용하여 새로운 액세스 토큰 및 리프레시 토큰을 재발급하는 서비스 클래스입니다.
 * 이 클래스는 Redis에 저장된 토큰의 유효성을 검증하고, 재발급을 처리하는 핵심 로직을 담당합니다.
 *
 * @see RedisRefreshTokenRepositoryPort Redis에 저장된 리프레시 토큰에 접근하기 위한 아웃고잉 포트
 * @see JwtTokenProviderPort JWT 토큰을 새로 발급하기 위한 아웃고잉 포트
 * @see JwtTokenResolverPort JWT 토큰의 내용을 추출하기 위한 아웃고잉 포트
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {
    private final RedisRefreshTokenRepositoryPort redisRefreshTokenRepositoryPort;
    private final JwtTokenProviderPort jwtTokenProviderPort;
    private final JwtTokenResolverPort jwtTokenResolverPort;

    /**
     * 사용자의 고유 ID를 사용하여 Redis에서 리프레시 토큰을 조회하고, 이를 기반으로 새로운 토큰을 재발급합니다.
     *
     * @param runnerId 토큰 재발급을 요청한 사용자의 고유 식별자(ID)입니다.
     * @return 새로운 액세스 토큰과 리프레시 토큰 정보를 담은 {@code TokenResult}를 발행하는 {@code Mono}입니다.
     * @throws BusinessException Redis에서 해당 사용자의 리프레시 토큰을 찾을 수 없을 때 발생합니다.
     */
    @Override
    public Mono<TokenResult> refreshToken(Long runnerId) {
        // Redis에서 리프레시 토큰을 찾습니다.
        return findRefreshTokenFromRedis(runnerId)
                // 찾아온 리프레시 토큰을 사용하여 토큰을 재발급합니다.
                .flatMap(this::reissueTokens);
    }

    /**
     * 주어진 리프레시 토큰을 해독하고, 사용자 정보를 추출하여 새로운 토큰 쌍을 재발급합니다.
     *
     * @param refreshToken 재발급에 사용될 리프레시 토큰 문자열입니다.
     * @return 새로운 토큰 정보를 담은 {@code TokenResult}를 발행하는 {@code Mono}입니다.
     */
    @Override
    public Mono<TokenResult> reissueTokens(String refreshToken) {
        // 리프레시 토큰에서 사용자 ID를 추출합니다. (블로킹 작업을 반응형으로 감싸 처리)
        return extractRunnerIdReactive(refreshToken)
                // 사용자 ID를 사용해서 Spring Security의 Authentication 객체를 만듭니다.
                .map(AuthenticationConverter::toAuthentication)
                // 만들어진 Authentication으로 기존 토큰을 삭제하고 새 토큰을 생성합니다.
                .flatMap(this::deleteOldTokenAndCreateNew);
    }

    /**
     * 리프레시 토큰에서 사용자 ID(Runner ID)를 추출하는 반응형 래퍼(Wrapper) 메서드입니다.
     *
     * @param refreshToken ID 추출에 사용될 리프레시 토큰 문자열입니다.
     * @return 토큰에서 추출된 사용자 ID를 발행하는 {@code Mono<Long>}입니다.
     * @implNote JWT 토큰 해독은 **블로킹(Blocking) 호출**이므로, {@code Schedulers.boundedElastic()}을 사용하여
     * 별도의 스레드에서 비동기적으로 처리되도록 합니다.
     */
    private Mono<Long> extractRunnerIdReactive(String refreshToken) {
        return Mono.fromCallable(() -> jwtTokenResolverPort.getIdFromToken(refreshToken))
                .subscribeOn(Schedulers.boundedElastic()); // 블로킹 호출을 별도 스레드풀에서
    }

    /**
     * 기존 리프레시 토큰을 Redis에서 삭제하고 새로운 토큰 쌍을 생성합니다.
     *
     * @param auth 토큰 재발급에 사용될 사용자 인증 정보({@code Authentication})입니다.
     * @return 새로운 토큰 정보를 담은 {@code TokenResult}를 발행하는 {@code Mono}입니다.
     */
    private Mono<TokenResult> deleteOldTokenAndCreateNew(Authentication auth) {
        Long runnerId = Long.valueOf(auth.getName());
        return redisRefreshTokenRepositoryPort
                // 기존의 리프레시 토큰을 삭제합니다.
                .deleteRefreshToken(runnerId)
                // 삭제 완료 후, 새로운 리프레시 토큰을 생성하고 저장합니다.
                .then(Mono.defer(() -> createAndSaveRefreshToken(auth, runnerId)));
    }

    /**
     * 사용자 ID를 사용하여 Redis에서 리프레시 토큰을 조회합니다.
     *
     * @param id 토큰을 조회할 사용자의 고유 식별자(ID)입니다.
     * @return Redis에서 조회된 리프레시 토큰 문자열을 발행하는 {@code Mono<String>}입니다.
     * @throws BusinessException Redis에 저장된 리프레시 토큰을 찾을 수 없을 경우 발생합니다.
     */
    private Mono<String> findRefreshTokenFromRedis(Long id) {
        return redisRefreshTokenRepositoryPort
                .getRefreshToken(id)
                .switchIfEmpty(
                        Mono.error(new BusinessException(
                                ErrorMessage.NOT_FOUND_REFRESH_TOKEN_IN_REDIS,
                                "리프레시 토큰을 찾을 수 없습니다.")
                        )
                );
    }

    /**
     * 새로운 액세스 및 리프레시 토큰을 생성하고, 새로운 리프레시 토큰을 Redis에 저장합니다.
     *
     * @param authentication 토큰 생성에 사용될 사용자 인증 정보입니다.
     * @param runnerId 토큰을 저장할 사용자의 고유 식별자(ID)입니다.
     * @return 새로운 토큰 쌍을 담은 {@code TokenResult}를 발행하는 {@code Mono}입니다.
     */
    private Mono<TokenResult> createAndSaveRefreshToken(Authentication authentication, Long runnerId) {
        Mono<String> accessTokenMono = Mono.fromCallable(() -> jwtTokenProviderPort.generateAccessToken(authentication))
                .subscribeOn(Schedulers.boundedElastic());
        Mono<RefreshTokenIssueResult> refreshTokenMono = Mono.fromCallable(() -> jwtTokenProviderPort.generateRefreshToken(authentication))
                .subscribeOn(Schedulers.boundedElastic());

        return accessTokenMono.zipWith(refreshTokenMono)
                .flatMap(tuple -> redisRefreshTokenRepositoryPort
                        .saveRefreshToken(runnerId, tuple.getT2().refreshToken())
                        .thenReturn(TokenResult.of(tuple.getT1(), tuple.getT2())));
    }
}