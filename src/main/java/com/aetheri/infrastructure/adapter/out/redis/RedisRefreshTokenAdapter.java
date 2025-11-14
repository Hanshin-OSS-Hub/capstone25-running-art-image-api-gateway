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
 * 비동기/논블로킹 방식으로 토큰을 저장, 조회, 삭제합니다.</p>
 */
@Service
public class RedisRefreshTokenAdapter implements RedisRefreshTokenRepositoryPort {
    private final ReactiveRedisTemplate<String, RefreshTokenMetadata> redisTemplate;
    private final JWTProperties jwtProperties;

    /**
     * {@code RedisRefreshTokenAdapter}의 생성자입니다.
     *
     * @param redisTemplate Redis와의 반응형 통신을 위한 템플릿입니다.
     * @param jwtProperties JWT 관련 설정 값들을 담고 있는 프로퍼티 객체입니다. (토큰 만료일 및 Redis 키 구성에 사용)
     */
    public RedisRefreshTokenAdapter(
            ReactiveRedisTemplate<String, RefreshTokenMetadata> redisTemplate,
            JWTProperties jwtProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 사용자 ID를 키로 사용하여 리프레시 토큰을 Redis에 저장합니다.
     *
     * <p>저장 시, JWT 설정에 정의된 리프레시 토큰 유효 기간({@code refreshTokenExpirationDays})에 맞춰 TTL(Time-To-Live)을 설정합니다.</p>
     *
     * @param userId 토큰 소유자의 고유 ID입니다.
     * @param refreshTokenMetadata 저장할 리프레시 토큰 문자열입니다.
     * @return 저장 성공 여부를 나타내는 {@code Mono<Boolean>}입니다.
     */
    @Override
    public Mono<Boolean> saveRefreshToken(String userId, RefreshTokenMetadata refreshTokenMetadata) {
        String key = buildKey(userId);
        // 설정된 일자만큼의 TTL을 Duration으로 계산
        Duration ttl = Duration.ofDays(jwtProperties.refreshTokenExpirationDays());

        return redisTemplate.opsForValue()
                .set(key, refreshTokenMetadata, ttl);
    }

    /**
     * 주어진 사용자 ID에 해당하는 리프레시 토큰을 Redis에서 조회합니다.
     *
     * @param refreshToken 조회할 토큰 소유자의 고유 ID입니다.
     * @return 조회된 리프레시 토큰 문자열을 발행하는 {@code Mono<String>}입니다. 토큰이 없으면 {@code Mono.empty()}를 발행합니다.
     */
    @Override
    public Mono<RefreshTokenMetadata> getRefreshToken(String refreshToken) {
        return redisTemplate.opsForValue().get(buildKey(refreshToken));
    }

    /**
     * 주어진 사용자 ID에 해당하는 리프레시 토큰을 Redis에서 삭제합니다.
     *
     * <p>주로 로그아웃 또는 회원 탈퇴 시 호출됩니다.</p>
     *
     * @param refreshToken 삭제할 토큰 소유자의 고유 ID입니다.
     * @return 삭제 성공 여부를 나타내는 {@code Mono<Boolean>}입니다.
     */
    @Override
    public Mono<Boolean> deleteRefreshToken(String refreshToken) {
        return redisTemplate.opsForValue().delete(buildKey(refreshToken));
    }


    /**
     * 주어진 사용자 ID를 기반으로 Redis에 저장할 고유 키 문자열을 생성합니다.
     *
     * <p>키 형식: {@code {prefix}:{refreshToken}:{suffix}}</p>
     *
     * @param refreshToken 키 생성에 사용할 사용자 ID입니다.
     * @return 생성된 Redis 키 문자열입니다.
     */
    private String buildKey(String refreshToken) {
        return jwtProperties.redis().key().prefix() + ":" + refreshToken + ":" + jwtProperties.redis().key().suffix();
    }
}