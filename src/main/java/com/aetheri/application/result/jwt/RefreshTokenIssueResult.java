package com.aetheri.application.result.jwt;

import lombok.AccessLevel;
import lombok.Builder;

import java.time.Instant;

/**
 * 리프레시 토큰 발급 결과를 나타내는 응답 레코드입니다.
 * 이 레코드는 발급된 리프레시 토큰 자체와 토큰의 고유 식별자(JTI), 그리고 발급 시각을 포함합니다.
 *
 * <p>이 레코드는 {@code @Builder(access = AccessLevel.PRIVATE)} 어노테이션이 적용되어 있으므로,
 * 정적 팩토리 메서드인 {@link #of(String, String, Instant)}를 통해 인스턴스를 생성해야 합니다.
 *
 * @param refreshToken 발급된 리프레시 토큰 문자열입니다. 이 토큰은 일반적으로 액세스 토큰이 만료되었을 때
 * 새로운 액세스 토큰을 얻는 데 사용됩니다.
 * @param jti JWT ID(JTI, JSON Web Token ID)입니다. 리프레시 토큰의 고유 식별자 역할을 합니다.
 * @param issuedAt 토큰이 발급된 시간(UTC 기준)을 나타내는 {@code Instant} 값입니다.
 */
@Builder(access = AccessLevel.PRIVATE)
public record RefreshTokenIssueResult(
        String refreshToken,
        String jti,
        Instant issuedAt
) {
    /**
     * 주어진 정보로 {@code RefreshTokenIssueResponse} 인스턴스를 생성하는 정적 팩토리 메서드입니다.
     * 이 메서드는 Lombok의 private 빌더를 활용하여 레코드 인스턴스를 안전하고 명시적으로 생성합니다.
     *
     * @param refreshToken 발급된 리프레시 토큰 문자열
     * @param jti JWT ID (JSON Web Token ID)
     * @param issuedAt 토큰이 발급된 시간
     * @return 새로 생성된 {@code RefreshTokenIssueResponse} 레코드 인스턴스
     */
    public static RefreshTokenIssueResult of(String refreshToken, String jti, Instant issuedAt) {
        return RefreshTokenIssueResult.builder()
                .refreshToken(refreshToken)
                .jti(jti)
                .issuedAt(issuedAt)
                .build();
    }
}