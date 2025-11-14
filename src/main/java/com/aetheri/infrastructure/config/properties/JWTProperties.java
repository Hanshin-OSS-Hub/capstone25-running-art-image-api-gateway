package com.aetheri.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * **JWT(JSON Web Token) 및 관련 보안 설정 값**을 외부 설정 파일(예: application.yml)로부터
 * 주입받기 위한 레코드 클래스입니다.
 *
 * <p>설정 파일에서 {@code jwt} 프리픽스({@code prefix = "jwt"})로 시작하는 속성들을
 * 이 레코드의 필드에 자동으로 바인딩합니다.</p>
 */
@ConfigurationProperties(prefix = "jwt")
public record JWTProperties(

        /**
         * JWT 액세스 토큰이 HTTP 헤더에 담길 때 사용될 헤더 이름입니다. (예: {@code Authorization})
         */
        String accessTokenHeader,

        /**
         * 리프레시 토큰이 저장될 HTTP 쿠키의 이름입니다. (예: {@code refresh-token})
         */
        String refreshTokenCookie,

        /**
         * 리프레시 토큰의 유효 기간(만료 기간)을 나타내는 **일(Day) 단위** 값입니다.
         */
        int refreshTokenExpirationDays,

        // 리프레쉬 토큰 바이트 길이
        int refreshTokenByteLength,

        /**
         * JWT 서명 및 검증에 사용될 비밀 키 문자열입니다. (반드시 **Base64 인코딩된** 문자열이어야 함)
         * <p>HS256(256bit) 알고리즘 사용 시 최소 32바이트(256비트) 이상을 권장합니다.</p>
         */
        String secret,

        /**
         * 액세스 토큰의 유효 기간(만료 기간)을 나타내는 **{@code Duration}** 값입니다. (일반적으로 시간 단위로 설정)
         */
        Duration accessTokenValidityInHour,

        /**
         * JWT의 {@code exp}, {@code nbf}, {@code iat} 클레임을 검증할 때 허용되는 **시계 오차(Clock Skew)** 범위입니다.
         * 단위는 초(Seconds)입니다.
         */
        int allowedClockSkewSeconds,

        /**
         * Spring Security의 권한(Authority)을 토큰 클레임에서 식별할 때 사용되는 키 이름입니다. (예: {@code roles})
         */
        String authorityKey,

        /**
         * 액세스 토큰이 HTTP 헤더에 담길 때 사용되는 스키마 이름입니다. (예: {@code Bearer})
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
     * @param maxToken 한 사용자당 저장 가능한 최대 토큰 개수입니다.
     * @param key Redis 키 구성에 사용되는 접두사(Prefix) 및 접미사(Suffix) 설정입니다.
     */
    public record Redis(
            int maxToken,
            Key key
    ) {
        /**
         * Redis에 저장되는 리프레시 토큰의 키 구성 요소입니다.
         *
         * <p>최종 키 형식: {@code {prefix}:{userId}:{suffix}}</p>
         *
         * @param prefix Redis 키의 접두사입니다. (예: {@code refresh-token})
         * @param suffix Redis 키의 접미사입니다. (예: {@code jti} 또는 토큰 타입)
         */
        public record Key(
                String prefix,
                String suffix
        ) {}
    }
}