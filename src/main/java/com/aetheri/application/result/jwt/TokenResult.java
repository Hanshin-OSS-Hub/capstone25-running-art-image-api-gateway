package com.aetheri.application.result.jwt;

/**
 * 서버 내부에서 사용되는 토큰 응답 레코드입니다.
 * 이 레코드는 새로운 액세스 토큰과, 함께 발급/갱신된 리프레시 토큰의 상세 정보를
 * {@link RefreshTokenIssueResult} 형태로 포함합니다.
 *
 * <p>이 DTO는 서버 내부에서 토큰 갱신 로직의 결과를 캡슐화하기 위해 사용되며,
 * 클라이언트(외부)에 직접 반환되지 않아야합니다.
 *
 * @param accessToken 새로 발급된 액세스 토큰 문자열입니다. 이 토큰은 실제 리소스 접근에 사용됩니다.
 * @param refreshTokenIssueResult 새로 발급된 리프레시 토큰에 대한 상세 응답 정보 레코드입니다.
 */
public record TokenResult(
        String accessToken,
        RefreshTokenIssueResult refreshTokenIssueResult
) {
    /**
     * 주어진 액세스 토큰과 리프레시 토큰 발급 응답으로 {@code TokenResponse} 인스턴스를 생성하는
     * 정적 팩토리 메서드입니다.
     *
     * @param accessToken 새로 발급된 액세스 토큰 문자열
     * @param refreshTokenIssueResult 리프레시 토큰 발급 상세 정보 레코드
     * @return 새로 생성된 {@code TokenResponse} 레코드 인스턴스
     */
    public static TokenResult of(String accessToken, RefreshTokenIssueResult refreshTokenIssueResult) {
        return new TokenResult(accessToken, refreshTokenIssueResult);
    }
}