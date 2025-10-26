package com.aetheri.infrastructure.adapter.out.jwt;

import com.aetheri.application.port.out.jwt.JwtTokenValidatorPort;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * JWT 토큰 유효성 검증 포트({@link JwtTokenValidatorPort})를 구현하는 컴포넌트입니다.
 * 이 클래스는 주어진 토큰의 서명(Signature)을 확인하고 구조적 유효성을 검사하여,
 * 토큰이 변조되지 않았고 만료되지 않았는지 등을 판단합니다.
 */
@Component
public class JwtTokenValidator implements JwtTokenValidatorPort {
    private final SecretKey KEY;

    /**
     * {@code JwtTokenValidator}의 생성자입니다.
     *
     * @param jwtKeyManager JWT 서명 키를 관리하는 컴포넌트입니다.
     */
    public JwtTokenValidator(JwtKeyManager jwtKeyManager) {
        // 토큰 검증에 필요한 서명 키를 KeyManager로부터 주입받습니다.
        this.KEY = jwtKeyManager.getKey();
    }

    /**
     * 주어진 JWT 토큰 문자열의 유효성을 검증합니다.
     *
     * <p>검증에는 토큰 서명 확인, 만료일 확인, 구조적 유효성 확인 등이 포함됩니다.</p>
     *
     * @param token 유효성을 검사할 JWT 토큰 문자열입니다.
     * @return 토큰이 유효하면 {@code true}를, 서명 오류, 만료, 구조적 오류 등으로 인해 유효하지 않으면 {@code false}를 반환합니다.
     */
    @Override
    public boolean validateToken(String token) {
        try {
            // Jwts.parserBuilder()를 사용하여 서명 키를 설정하고 토큰을 파싱 및 검증합니다.
            Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
            // 예외 없이 완료되면 유효한 토큰입니다.
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException: 서명 오류, 만료(ExpiredJwtException), 구조적 오류 등
            // IllegalArgumentException: 토큰 문자열이 null이거나 비어있을 때
            // 토큰 유효성 검증 실패 시 예외를 무시하고 false 반환
            return false;
        }
    }
}