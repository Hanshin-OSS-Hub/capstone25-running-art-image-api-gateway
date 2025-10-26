package com.aetheri.infrastructure.adapter.in.web.dto.internal.jwt;

import com.aetheri.application.result.jwt.TokenResult;
import io.swagger.v3.oas.annotations.Hidden;

/**
 * 서버 내부에서 사용되는 토큰 응답 레코드입니다.
 * 이 레코드는 새로운 액세스 토큰과, 함께 발급/갱신된 리프레시 토큰의 상세 정보를
 * {@link RefreshTokenIssueResponse} 형태로 포함합니다.
 *
 * <p>이 DTO는 서버 내부에서 토큰 갱신 로직의 결과를 캡슐화하기 위해 사용되며,
 * 클라이언트(외부)에 직접 반환되지 않아야합니다.
 *
 * @param accessToken 새로 발급된 액세스 토큰 문자열입니다. 이 토큰은 실제 리소스 접근에 사용됩니다.
 * @param refreshTokenIssueResponse 새로 발급된 리프레시 토큰에 대한 상세 응답 정보 레코드입니다.
 * @see com.aetheri.application.result.jwt.TokenResult
 */
@Hidden
public record TokenResponse(
        String accessToken,
        RefreshTokenIssueResponse refreshTokenIssueResponse
) {
    public static TokenResponse toResponse(
            TokenResult tokenResult
    ) {
        return new TokenResponse(
                tokenResult.accessToken(),
                RefreshTokenIssueResponse.toResponse(tokenResult.refreshTokenIssueResult())
        );
    }
}