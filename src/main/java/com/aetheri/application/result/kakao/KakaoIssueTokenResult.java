package com.aetheri.application.result.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오(Kakao) OAuth 2.0 API 서버로부터 토큰을 성공적으로 발급받았을 때의 응답 구조를 위한 레코드입니다.
 * 이 레코드는 카카오 서버가 반환하는 액세스 토큰 및 리프레시 토큰 정보를 캡슐화합니다.
 *
 * <p>이 DTO는 외부 서비스(카카오)와의 통신을 위한 내부 모델이며,
 * 보안상의 이유로 클라이언트(서비스 사용자)에게 직접 노출되어서는 안 됩니다.
 *
 * @param tokenType 토큰 타입, bearer로 고정
 * @param accessToken 갱신된 사용자 액세스 토큰 값
 * @param expiresIn 액세스 토큰 만료 시간(초)
 * @param refreshToken 갱신된 사용자 리프레시 토큰 값, 기존 리프레시 토큰의 유효기간이 1개월 미만인 경우에만 갱신
 * @param refreshTokenExpiresIn 리프레시 토큰 만료 시간(초
 * @param scope 인증된 사용자의 정보 조회 권한 범위 범위가 여러 개일 경우, 공백으로 구분 - 참고: OpenID Connect가 활성화된 앱의 토큰 발급 요청인 경우, ID 토큰이 함께 발급되며 scope 값에 openid 포함
 */
public record KakaoIssueTokenResult(
        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Integer expiresIn,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("refresh_token_expires_in")
        Integer refreshTokenExpiresIn,

        @JsonProperty("scope")
        String scope
) {
}