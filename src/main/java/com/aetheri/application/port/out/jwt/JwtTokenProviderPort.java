package com.aetheri.application.port.out.jwt;

import com.aetheri.application.result.jwt.RefreshTokenIssueResult;
import org.springframework.security.core.Authentication;

/**
 * JWT(JSON Web Token)를 생성하는 아웃고잉 포트(Port)입니다.
 * 이 포트는 시스템의 비즈니스 로직(유즈케이스)이 토큰 생성 구현체(어댑터)에 의존하지 않도록 분리하는 역할을 합니다.
 *
 * @see com.aetheri.infrastructure.adapter.out.jwt.JwtTokenProvider 실제 구현체(어댑터)의 예시입니다.
 */
public interface JwtTokenProviderPort {
    /**
     * 주어진 인증 정보({@code Authentication})를 기반으로 액세스 토큰(Access Token)을 생성합니다.
     *
     * @param authentication 토큰을 생성하는 데 필요한 사용자 인증 및 권한 정보 객체입니다.
     * @return 생성된 액세스 토큰 문자열입니다.
     */
    String generateAccessToken(Authentication authentication);

    /**
     * 주어진 인증 정보({@code Authentication})를 기반으로 리프레시 토큰(Refresh Token)을 생성하고,
     * 토큰 자체와 추가 정보(JTI, 발급 시각 등)를 포함하여 응답합니다.
     *
     * @param authentication 토큰을 생성하는 데 필요한 사용자 인증 및 권한 정보 객체입니다.
     * @return 생성된 리프레시 토큰 문자열과 관련 메타데이터를 담은 {@code RefreshTokenIssueResult} 객체입니다.
     * @see RefreshTokenIssueResult 리프레시 토큰 발급 응답에 대한 상세 정보
     */
    RefreshTokenIssueResult generateRefreshToken(Authentication authentication);
}