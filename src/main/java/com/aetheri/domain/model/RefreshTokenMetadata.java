package com.aetheri.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * Redis Value에 저장될 리프레시 토큰의 상태 정보입니다.
 * (Java 17+의 Record를 사용하면 더 간결합니다)
 */
@Getter
@RequiredArgsConstructor
public class RefreshTokenMetadata {

    private final String userId;
    private final boolean isValid; // 탈취 감지 시 false로 설정
    private final Instant expiresAt;

    // 탈취 감지를 위한 상태 변경 메서드
    public RefreshTokenMetadata invalidated() {
        return new RefreshTokenMetadata(this.userId, false, this.expiresAt);
    }

    public static RefreshTokenMetadata of(String userId, Instant expiresAt) {
        return new RefreshTokenMetadata(userId, true, expiresAt);
    }
}