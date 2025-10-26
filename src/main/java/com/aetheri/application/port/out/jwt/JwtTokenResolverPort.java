package com.aetheri.application.port.out.jwt;

import java.util.List;

/**
 * JWT(JSON Web Token) 내부의 클레임(Claim) 정보(예: ID, 권한)를 추출하는 아웃고잉 포트(Port)입니다.
 * 이 포트는 시스템의 비즈니스 로직(유즈케이스)이 토큰 해독 및 검증 구현체에 의존하지 않도록 분리하는 역할을 합니다.
 *
 * @see com.aetheri.infrastructure.adapter.out.jwt.JwtTokenResolver 실제 구현체(어댑터)의 예시입니다.
 */
public interface JwtTokenResolverPort {

    /**
     * 주어진 JWT 문자열에서 사용자의 고유 식별자(ID)를 추출합니다.
     *
     * @param token 정보를 추출할 JWT 문자열입니다.
     * @return 토큰에 포함된 사용자의 고유 식별자(ID)입니다.
     */
    Long getIdFromToken(String token);

    /**
     * 주어진 JWT 문자열에서 사용자에게 할당된 권한(Role) 목록을 추출합니다.
     *
     * @param token 정보를 추출할 JWT 문자열입니다.
     * @return 토큰에 포함된 권한 문자열 목록({@code List<String>})입니다.
     */
    List<String> getRolesFromToken(String token);
}