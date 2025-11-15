package com.aetheri.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT(JSON Web Token) 및 관련 보안 설정 값을 관리하는 타입-세이프(Type-Safe) 속성 클래스입니다.
 * <p>
 * 이 클래스는 {@code application.yml} (또는 {@code .properties}) 파일의
 * {@code "jwt"} 접두사를 가진 속성들을 이 레코드의 필드에 자동으로 바인딩합니다.
 *
 * @param accessTokenHeader           액세스 토큰을 담을 HTTP 헤더 이름 (예: "Authorization")
 * @param refreshTokenCookie          리프레시 토큰을 저장할 HTTP 쿠키 이름 (예: "refresh-token")
 * @param refreshTokenExpirationDays  리프레시 토큰의 유효 기간(일 단위)
 * @param refreshTokenByteLength      새 리프레시 토큰 생성 시 사용될 무작위 바이트 배열의 길이 (Base64 인코딩 전 기준)
 * @param secret                      JWT 서명 및 검증용 Base64 인코딩 비밀 키
 * @param accessTokenValidityInHour   액세스 토큰의 유효 기간 (예: "30m", "1h")
 * @param allowedClockSkewSeconds     JWT 시간(exp, nbf, iat) 검증 시 허용할 시계 오차(초 단위)
 * @param authorityKey                토큰 클레임 내 Spring Security 권한을 식별하는 키 (예: "auth")
 * @param bearerHeader                액세스 토큰 전송 시 사용할 HTTP 인증 스키마 (일반적으로 "Bearer")
 * @param redis                       리프레시 토큰 저장을 위한 Redis 세부 설정
 */
@ConfigurationProperties(prefix = "jwt")
public record JWTProperties(

        /**
         * JWT 액세스 토큰이 HTTP 헤더에 담길 때 사용될 헤더 이름입니다.
         * (예: {@code Authorization})
         */
        String accessTokenHeader,

        /**
         * 리프레시 토큰이 저장될 HTTP 쿠키의 이름입니다.
         * (예: {@code refresh-token})
         */
        String refreshTokenCookie,

        /**
         * 리프레시 토큰의 유효 기간(만료 기간)을 나타내는 일(Day) 단위 값입니다.
         */
        int refreshTokenExpirationDays,

        /**
         * 새로운 리프레시 토큰(무작위 문자열) 생성 시 사용될 바이트 배열의 길이입니다.
         * <p>
         * 이 바이트 배열은 일반적으로 Base64로 인코딩되어 최종 토큰 문자열이 됩니다.
         */
        int refreshTokenByteLength,

        /**
         * JWT 서명 및 검증에 사용되는 비밀 키(Secret Key)입니다.
         * <p>
         * <b>반드시 Base64 인코딩된</b> 문자열이어야 합니다.
         * HS256 알고리즘 사용 시, 256비트(32바이트) 이상의 강력한 키를 권장합니다.
         */
        String secret,

        /**
         * 액세스 토큰의 유효 기간(만료 기간)입니다.
         * <p>
         * Spring의 {@link Duration} 형식으로 파싱됩니다. (예: {@code "30m"}, {@code "1h"})
         */
        Duration accessTokenValidityInHour,

        /**
         * JWT의 만료(exp), 발급(iat), 활성(nbf) 시간 클레임을 검증할 때 허용되는
         * 시계 오차(Clock Skew) 범위입니다. (단위: 초)
         */
        int allowedClockSkewSeconds,

        /**
         * Spring Security 권한(Authority)을 식별하기 위해 토큰 클레임(Claim)에 사용될 키 이름입니다.
         * (예: {@code "auth"})
         */
        String authorityKey,

        /**
         * 액세스 토큰 전송 시 사용되는 HTTP 인증 스키마(Scheme)입니다.
         * <p>
         * 일반적으로 {@code "Bearer"}가 사용되며, 헤더 값은 {@code "Bearer {token}"} 형식이 됩니다.
         */
        String bearerHeader,

        /**
         * 리프레시 토큰 저장을 위한 Redis 관련 세부 설정입니다.
         */
        Redis redis

) {
    /**
     * 리프레시 토큰 관리를 위한 Redis 관련 설정 세부 사항입니다.
     *
     * @param maxToken 한 사용자(또는 세션)당 Redis에 저장 가능한 최대 리프레시 토큰 개수입니다.
     * @param key      Redis 키 구성에 사용되는 접두사(Prefix) 및 접미사(Suffix) 설정입니다.
     */
    public record Redis(
            int maxToken,
            Key key
    ) {
        /**
         * Redis에 저장되는 리프레시 토큰의 키(Key) 구성 요소입니다.
         * <p>
         * 서비스 구현에 따라 최종 키 형식이 (예: {@code {prefix}:{tokenId}:{suffix}})
         * 또는 (예: {@code {prefix}:{userId}:{suffix}}) 등으로 조합될 수 있습니다.
         *
         * @param prefix Redis 키의 접두사입니다. (예: {@code "runner"})
         * @param suffix Redis 키의 접미사입니다. (예: {@code "token_rts"})
         */
        public record Key(
                String prefix,
                String suffix
        ) {}
    }
}