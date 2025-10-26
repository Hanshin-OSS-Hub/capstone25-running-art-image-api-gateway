package com.aetheri.application.result.kakao;

/**
 * 로그인 성공 후 토큰 정보를 처리 핸들러에게 응답하기 위한 내부 DTO입니다.
 * 이 레코드는 발급된 액세스 토큰, 리프레시 토큰, 그리고 리프레시 토큰의 만료 시간을 캡슐화합니다.
 *
 * <p>이 DTO는 서버 내부 로직을 위한 모델이며, 클라이언트(외부)에 직접 JSON 본문으로 반환되어서는 안 됩니다.
 * 액세스 토큰은 응답 헤더에, 리프레시 토큰은 HTTP Only 쿠키에 설정되어 클라이언트에게 전달되어야 합니다.
 *
 * @param accessToken 발급된 액세스 토큰 문자열입니다. 리소스 접근에 사용되며, 응답 헤더에 담겨야 합니다.
 * @param refreshToken 발급된 리프레시 토큰 문자열입니다. 액세스 토큰 갱신에 사용되며, HTTP Only 쿠키에 담겨야 합니다.
 * @param refreshTokenExpirationTime 리프레시 토큰의 유효 기간(밀리초)입니다.
 * 이 값은 쿠키의 만료 시간 설정에 사용됩니다.
 */
public record SignInResult(
        String accessToken,
        String refreshToken,
        long refreshTokenExpirationTime
) {
}