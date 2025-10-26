package com.aetheri.infrastructure.adapter.out.jwt;

import com.aetheri.application.port.out.jwt.JwtTokenResolverPort;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JWT 토큰의 내용을 안전하게 파싱하고 추출하기 위한 포트({@link JwtTokenResolverPort})의 구현체입니다.
 * 이 컴포넌트는 {@link JwtKeyManager}로부터 미리 설정된 {@link JwtParser}를 사용하여 토큰을 검증 및 해독합니다.
 */
@Component
public class JwtTokenResolver implements JwtTokenResolverPort {
    private final JwtParser parser;

    /**
     * {@code JwtTokenResolver}의 생성자입니다.
     *
     * @param jwtKeyManager JWT 서명 키와 파서를 관리하는 컴포넌트입니다.
     */
    public JwtTokenResolver(JwtKeyManager jwtKeyManager) {
        // 미리 서명 키가 설정된 JwtParser 인스턴스를 주입받습니다.
        this.parser = jwtKeyManager.getParser();
    }

    /**
     * 주어진 JWT 토큰에서 사용자 고유 식별자(ID)를 추출합니다.
     *
     * <p>사용자 ID는 토큰의 {@code Subject} 클레임에서 가져오며, 이를 {@code Long} 타입으로 변환합니다.</p>
     *
     * @param token 사용자 ID를 추출할 JWT 토큰 문자열입니다.
     * @return 토큰에서 추출된 사용자 ID({@code Long})입니다.
     * @throws BusinessException {@code Subject} 클레임이 숫자가 아니어서 변환에 실패할 경우 {@code JWT_SUBJECT_IS_NOT_NUMBER} 예외를 발생시킵니다.
     */
    // 토큰에서 사용자 ID 추출
    @Override
    public Long getIdFromToken(String token) {
        String sub = claims(token).getSubject();
        try {
            return Long.valueOf(sub);
        } catch (NumberFormatException e) {
            throw new BusinessException(
                    ErrorMessage.JWT_SUBJECT_IS_NOT_NUMBER,
                    "JWT 토큰 값이 유효하지 않습니다."
            );
        }
    }

    /**
     * 주어진 JWT 토큰에서 사용자 권한 목록({@code roles})을 추출합니다.
     *
     * <p>권한 정보는 {@code roles} 커스텀 클레임에서 가져옵니다. 해당 클레임이 없으면 빈 목록을 반환합니다.</p>
     *
     * @param token 권한 목록을 추출할 JWT 토큰 문자열입니다.
     * @return 토큰에서 추출된 권한 문자열 목록입니다.
     */
    // 권한 가져오기
    @Override
    public List<String> getRolesFromToken(String token) {
        // 커스텀 클레임 "roles"를 List.class 타입으로 가져옵니다.
        List<?> roles = claims(token).get("roles", List.class);

        return roles == null ?
                List.of() : // 클레임이 없으면 빈 목록 반환
                roles.stream().map(String::valueOf).toList(); // 요소를 String으로 변환하여 목록 반환
    }

    /**
     * 주어진 JWT 토큰을 파싱하고 서명을 검증하여 {@link Claims}(페이로드)를 추출합니다.
     *
     * @param token 파싱할 JWT 토큰 문자열입니다.
     * @return 토큰의 페이로드 정보를 담고 있는 {@code Claims} 객체입니다.
     * @throws BusinessException 토큰 문자열이 {@code null}이거나 비어있을 경우 {@code JWT_TOKEN_IS_EMPTY} 예외를 발생시킵니다.
     * 파싱 및 서명 검증 실패 시, JWT 라이브러리 예외({@code JwtException})가 발생할 수 있습니다.
     */
    private Claims claims(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(
                    ErrorMessage.JWT_TOKEN_IS_EMPTY,
                    "JWT 토큰이 비어있습니다."
            );
        }
        // parser를 사용하여 토큰을 해독하고 서명 검증을 수행
        return parser.parseClaimsJws(token).getBody();
    }
}