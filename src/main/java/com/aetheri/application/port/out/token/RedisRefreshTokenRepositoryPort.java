package com.aetheri.application.port.out.token;

import com.aetheri.domain.model.RefreshTokenMetadata;
import reactor.core.publisher.Mono;

/**
 * Redis 저장소를 통해 리프레시 토큰(Refresh Token)의 관리(저장, 조회, 삭제)를 담당하는 아웃고잉 포트(Port)입니다.
 * 이 포트는 고성능의 토큰 처리를 위해 Redis를 활용하는 외부 통신 구현체에 대한 추상화를 제공합니다.
 *
 * @see com.aetheri.infrastructure.adapter.out.redis.RedisRefreshTokenAdapter 실제 Redis 기반 토큰 관리 구현체(어댑터)의 예시입니다.
 */
public interface RedisRefreshTokenRepositoryPort {

    /**
     * 주어진 사용자 ID를 키로 사용하여 리프레시 토큰을 Redis에 저장합니다.
     *
     * <p>이 메서드는 토큰을 저장하고 적절한 만료 시간(TTL)을 설정하는 역할을 포함합니다.
     * 일반적으로 사용자의 ID를 키로, 리프레시 토큰 문자열을 값으로 저장합니다.</p>
     *
     * @param key 리프레쉬 토큰 본문입니다
     * @param value {@code RefreshTokenMetadata} 리프레쉬 토큰 메타데이터입니다. 이는 Value 값으로 사용됩니다.
     * @return 저장 작업의 성공 여부({@code true} 또는 {@code false})를 발행하는 {@code Mono<Boolean>} 객체입니다.
     */
    Mono<Boolean> saveRefreshToken(String key, RefreshTokenMetadata value);

    /**
     * 주어진 사용자 ID를 사용하여 Redis에 저장된 리프레시 토큰을 조회합니다.
     *
     * @param key 리프레쉬 토큰 본문입니다
     * @return Redis에서 {@code RefreshTokenMetadata}를 조회합니다.
     */
    Mono<RefreshTokenMetadata> getRefreshToken(String key);

    /**
     * 주어진 사용자 ID와 연결된 Redis의 리프레시 토큰을 삭제합니다.
     *
     * @param key 리프레쉬 토큰 본문입니다
     * @return 삭제 작업의 성공 여부({@code true} 또는 {@code false})를 발행하는 {@code Mono<Boolean>} 객체입니다.
     */
    Mono<Boolean> deleteRefreshToken(String key);

    Mono<Void> updateRefreshToken(String refreshToken, RefreshTokenMetadata metadata);
}