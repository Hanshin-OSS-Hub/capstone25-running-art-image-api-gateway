package com.aetheri.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    // 정적 팩토리 메서드
    public static RefreshTokenMetadata of(String userId, Instant expiresAt) {
        return new RefreshTokenMetadata(userId, true, expiresAt);
    }

    @JsonIgnore
    public boolean isValidAndNotExpired() {
        return this.isValid && this.expiresAt.isAfter(Instant.now());
    }
}