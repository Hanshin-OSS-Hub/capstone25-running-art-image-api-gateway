package com.aetheri.application.result.kakao;

public record KakaoTokenAndIdResult(
        // 카카오에서 발급한 액세스 토큰
        String accessToken,
        // 카카오에서 발급한 리프레시 토큰
        String refreshToken,
        // 사용자의 시스템 ID (로그인 후 갱신됨)
        Long id,
        // 사용자의 닉네임
        String name
) {
}