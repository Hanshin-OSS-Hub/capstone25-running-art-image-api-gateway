package com.aetheri.application.port.out.jwt;

/**
 * JWT(JSON Web Token)의 유효성 검증 기능을 담당하는 아웃고잉 포트(Port)입니다.
 * 이 포트는 토큰의 서명(Signature) 유효성, 만료 여부 등 다양한 검증 규칙을 적용하여
 * 토큰이 정상적으로 사용 가능한지 확인하는 역할을 합니다.
 *
 * @see com.aetheri.infrastructure.adapter.out.jwt.JwtTokenValidator 실제 유효성 검증 구현체(어댑터)의 예시입니다.
 */
public interface JwtTokenValidatorPort {

    /**
     * 주어진 JWT 문자열의 유효성(Validity)을 검사합니다.
     *
     * <p>검사에는 토큰의 서명(Signature)이 올바른지, 토큰이 만료되지 않았는지,
     * 필수 클레임(Claim)이 포함되어 있는지 등의 확인 과정이 포함됩니다.</p>
     *
     * @param token 유효성을 검사할 JWT 문자열입니다.
     * @return 토큰이 유효한 경우 {@code true}를, 유효하지 않은 경우 {@code false}를 반환합니다.
     */
    boolean validateToken(String token);
}