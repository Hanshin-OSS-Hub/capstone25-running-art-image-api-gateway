package com.aetheri.domain.model;

import java.time.Instant;

/**
 * Redis Value에 저장될 리프레시 토큰의 상태 정보입니다.
 * (Java 17+의 Record를 사용하면 더 간결합니다)
 */
public record RefreshTokenMetadata(
        String userId,
        boolean isValid, // 탈취 감지 시 false로 설정
        Instant expiresAt
) {
    // 탈취 감지를 위한 상태 변경 메서드 (불변성 유지)
    // Record는 Setter를 가지지 않으므로, 상태 변경 시 새로운 Record 인스턴스를 반환합니다.
    public RefreshTokenMetadata invalidate() {
        return new RefreshTokenMetadata(this.userId, false, this.expiresAt);
    }

    // 정적 팩토리 메서드
    public static RefreshTokenMetadata of(String userId, Instant expiresAt) {
        return new RefreshTokenMetadata(userId, true, expiresAt);
    }

    public boolean isValidAndNotExpired() {
        return this.isValid && this.expiresAt.isAfter(Instant.now());
    }
}