package com.aetheri.application.result.kakao;

import lombok.Builder;

@Builder
public record KakaoTokenResult(
     Long id,
     Long runnerId,
     String accessToken,
     String refreshToken
) {
}
