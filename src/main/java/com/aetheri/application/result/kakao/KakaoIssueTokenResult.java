package com.aetheri.application.result.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오(Kakao) OAuth 2.0 API 서버로부터 토큰을 성공적으로 발급받았을 때의 응답 구조를 위한 레코드입니다.
 * 이 레코드는 카카오 서버가 반환하는 액세스 토큰 및 리프레시 토큰 정보를 캡슐화합니다.
 *
 * <p>이 DTO는 외부 서비스(카카오)와의 통신을 위한 내부 모델이며,
 * 보안상의 이유로 클라이언트(서비스 사용자)에게 직접 노출되어서는 안 됩니다.
 *
 * @param accessToken 실제로 리소스 서버에 접근할 때 사용되는 토큰 문자열입니다. JSON 필드명은 {@code access_token}입니다.
 * @param expiresIn 액세스 토큰의 유효 기간(초)입니다. JSON 필드명은 {@code expires_in}입니다.
 * @param refreshToken 액세스 토큰 만료 시 재발급에 사용되는 토큰 문자열입니다. JSON 필드명은 {@code refresh_token}입니다.
 * @param refreshTokenExpiresIn 리프레시 토큰의 유효 기간(초)입니다. JSON 필드명은 {@code refresh_token_expires_in}입니다.
 */
public record KakaoIssueTokenResult(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Integer expiresIn,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("refresh_token_expires_in")
        Integer refreshTokenExpiresIn
) {
}