package com.aetheri.infrastructure.adapter.out.redis;

import com.aetheri.application.port.out.token.RedisRefreshTokenRepositoryPort;
import com.aetheri.domain.model.RefreshTokenMetadata;
import com.aetheri.infrastructure.config.properties.JWTProperties;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * {@code Redis}를 사용하여 리프레시 토큰(Refresh Token) 데이터를 관리하는
 * 반응형 데이터 접근 포트({@link RedisRefreshTokenRepositoryPort})의 구현체입니다.
 *
 * <p>이 어댑터는 Spring Data Redis의 {@link ReactiveRedisTemplate}를 사용하여
 * 비동기/논블로킹 방식으로 토큰을 저장, 조회, 삭제, 그리고 상태 업데이트를 처리합니다.</p>
 */
@Service
public class RedisRefreshTokenAdapter implements RedisRefreshTokenRepositoryPort {
    private final ReactiveRedisTemplate<String, RefreshTokenMetadata> reactiveRedisTemplate;
    private final JWTProperties jwtProperties;

    /**
     * {@code RedisRefreshTokenAdapter}의 생성자입니다.
     *
     * <p>의존성 주입을 통해 {@link ReactiveRedisTemplate}와 {@link JWTProperties} 객체를 받아
     * Redis 통신 및 TTL 설정에 사용합니다.</p>
     *
     * @param reactiveRedisTemplate Redis와의 반응형 통신을 위한 템플릿입니다.
     * @param jwtProperties JWT 관련 설정 값들을 담고 있는 프로퍼티 객체입니다. (토큰 만료일 및 Redis 키 구성에 사용)
     */
    public RedisRefreshTokenAdapter(
            ReactiveRedisTemplate<String, RefreshTokenMetadata> reactiveRedisTemplate,
            JWTProperties jwtProperties
    ) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 새로운 리프레시 토큰 메타데이터를 Redis에 저장합니다.
     *
     * <p>저장 시, JWT 설정에 정의된 리프레시 토큰 유효 기간({@code refreshTokenExpirationDays})에 맞춰
     * TTL(Time-To-Live)을 설정합니다. Redis Key는 {@code buildKey}를 통해 생성됩니다.</p>
     *
     * @param refreshToken 토큰 문자열 자체 (Redis Key의 일부로 사용됨).
     * @param refreshTokenMetadata 저장할 리프레시 토큰의 상태 정보({@code userId}, {@code isValid}, {@code expiresAt})입니다.
     * @return 저장 성공 여부를 나타내는 {@code Mono<Boolean>}입니다.
     */
    @Override
    public Mono<Boolean> saveRefreshToken(String refreshToken, RefreshTokenMetadata refreshTokenMetadata) {
        String key = buildKey(refreshToken);
        // 설정된 일자만큼의 TTL을 Duration으로 계산
        Duration ttl = Duration.ofDays(jwtProperties.refreshTokenExpirationDays());

        return reactiveRedisTemplate.opsForValue()
                .set(key, refreshTokenMetadata, ttl);
    }

    /**
     * 주어진 리프레시 토큰 문자열에 해당하는 메타데이터를 Redis에서 조회합니다.
     *
     * <p>Redis Key는 {@code buildKey}를 통해 생성됩니다. 조회 결과가 없으면 {@code Mono.empty()}를 발행합니다.</p>
     *
     * @param refreshToken 조회할 리프레시 토큰 문자열입니다.
     * @return 조회된 {@code RefreshTokenMetadata}를 발행하는 {@code Mono<RefreshTokenMetadata>}입니다.
     */
    @Override
    public Mono<RefreshTokenMetadata> getRefreshToken(String refreshToken) {
        return reactiveRedisTemplate.opsForValue().get(buildKey(refreshToken));
    }

    /**
     * 주어진 리프레시 토큰에 해당하는 데이터를 Redis에서 삭제합니다.
     *
     * <p>주로 리프레시 토큰 재발급 시 기존 토큰 무효화(로테이션) 과정에서 호출됩니다.</p>
     *
     * @param refreshToken 삭제할 리프레시 토큰 문자열입니다.
     * @return 삭제 성공 여부를 나타내는 {@code Mono<Boolean>}입니다.
     */
    @Override
    public Mono<Boolean> deleteRefreshToken(String refreshToken) {
        return reactiveRedisTemplate.opsForValue().delete(buildKey(refreshToken));
    }

    /**
     * 기존 리프레시 토큰의 메타데이터를 업데이트합니다. (주로 탈취 감지 시 무효화 상태로 변경)
     *
     * <p>이 구현은 토큰이 무효화({@code metadata.invalidate()})될 때, 기존 Redis 키의 **남아있는 TTL을 유지**하며 값을 덮어씁니다.</p>
     *
     * @param refreshToken 업데이트할 리프레시 토큰 문자열입니다.
     * @param metadata 업데이트할 새로운 메타데이터입니다. (서비스 레이어에서 이미 무효화된 상태로 전달될 것을 가정)
     * @return 작업 완료를 나타내는 {@code Mono<Void>}입니다.
     */
    @Override
    public Mono<Void> updateRefreshToken(String refreshToken, RefreshTokenMetadata metadata) {
        // 1. 기존 TTL을 먼저 조회합니다.
        Mono<Long> ttlMono = reactiveRedisTemplate.getExpire(refreshToken)
                .map(Duration::getSeconds);

        // 2. TTL을 사용하여 새 값(무효화된 메타데이터)을 저장합니다.
        return ttlMono.flatMap(ttl -> {
            if (ttl > 0) {
                // TTL이 남아있다면 새 값으로 덮어쓰고 TTL을 유지합니다.
                return reactiveRedisTemplate.opsForValue()
                        .set(refreshToken, metadata.invalidate(), Duration.ofSeconds(ttl))
                        .then();
            } else {
                // TTL이 없거나 곧 만료된다면, 단순 덮어쓰기를 합니다.
                // (보안상 곧 사라질 토큰이므로 상관 없음)
                return reactiveRedisTemplate.opsForValue()
                        .set(refreshToken, metadata.invalidate())
                        .then();
            }
        });
    }


    /**
     * 리프레시 토큰 문자열을 기반으로 Redis에 저장할 고유 키 문자열을 생성합니다.
     *
     * <p>키 형식은 설정 파일({@code JWTProperties})에 정의된 프리픽스와 서픽스를 사용하여 다음과 같이 구성됩니다:</p>
     * <p>{@code {prefix}:{refreshToken}:{suffix}}</p>
     *
     * @param refreshToken 키 생성에 사용할 리프레시 토큰 문자열입니다.
     * @return 생성된 Redis 키 문자열입니다.
     */
    private String buildKey(String refreshToken) {
        return jwtProperties.redis().key().prefix() + ":" + refreshToken + ":" + jwtProperties.redis().key().suffix();
    }
}