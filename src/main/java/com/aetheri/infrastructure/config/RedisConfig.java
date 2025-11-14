package com.aetheri.infrastructure.config;

import com.aetheri.domain.model.RefreshTokenMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;

/**
 * Spring Data Redis의 반응형(Reactive) 설정 클래스
 *
 * <p>이 클래스는 {@link ReactiveRedisConnectionFactory}를 활용하여 Redis와의
 * 비동기/논블로킹(Async/Non-blocking) 통신을 위한 기반을 마련합니다.</p>
 *
 * <p>주요 목적은 String 키와 도메인 객체인 {@code RefreshTokenMetadata} 값을
 * 처리하도록 특화된 {@link ReactiveRedisTemplate} 빈을 정의하는 것입니다.</p>
 */
@Configuration
public class RedisConfig {

    /**
     * RefreshTokenMetadata를 위한 Primary ReactiveRedisTemplate 빈 정의
     *
     * <p>Redis 연결 팩토리를 사용하여 {@code String} 키와 JSON 직렬화된
     * {@code RefreshTokenMetadata} 값을 처리하는 주요(Primary) {@link ReactiveRedisTemplate} 빈을 생성합니다.</p>
     *
     * <ul>
     * <li>Key Serializer: {@link StringRedisSerializer}를 사용하여 Key는 문자열로 저장됩니다.</li>
     * <li>Value Serializer: {@link Jackson2JsonRedisSerializer}를 사용하여 Value는 JSON 형식으로 직렬화/역직렬화됩니다.</li>
     * </ul>
     *
     * @param factory Redis 연결을 관리하는 반응형 연결 팩토리입니다.
     * @return String 키와 RefreshTokenMetadata 값을 위한 설정이 완료된 {@code ReactiveRedisTemplate} 인스턴스입니다.
     */
    @Bean
    @Primary
    public ReactiveRedisTemplate<String, RefreshTokenMetadata> refreshTokenMetadataReactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        // Key 직렬화 (String)
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        // Value 직렬화 (JSON - RefreshTokenMetadata 객체)
        // Jackson2JsonRedisSerializer를 사용하여 Java 객체(RefreshTokenMetadata)를 JSON으로 변환합니다.
        Jackson2JsonRedisSerializer<RefreshTokenMetadata> valueSerializer =
                new Jackson2JsonRedisSerializer<>(RefreshTokenMetadata.class);

        // RedisSerializationContext 생성
        // Key는 String, Value는 JSON으로 직렬화하도록 Context를 설정합니다.
        RedisSerializationContext<String, RefreshTokenMetadata> context =
                RedisSerializationContext.<String, RefreshTokenMetadata>newSerializationContext()
                        .key(keySerializer)             // 일반 Key 직렬화 방식
                        .value(valueSerializer)         // 일반 Value 직렬화 방식
                        .hashKey(keySerializer)         // Hash 타입의 Key 직렬화 방식
                        .hashValue(valueSerializer)     // Hash 타입의 Value 직렬화 방식
                        .build();

        // ReactiveRedisTemplate 인스턴스 생성 및 반환
        return new ReactiveRedisTemplate<>(factory, context);
    }
}