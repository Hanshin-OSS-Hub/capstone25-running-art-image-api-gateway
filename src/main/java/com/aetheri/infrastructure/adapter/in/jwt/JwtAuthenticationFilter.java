package com.aetheri.infrastructure.adapter.in.jwt;

import com.aetheri.application.port.out.jwt.JwtTokenResolverPort;
import com.aetheri.application.port.out.jwt.JwtTokenValidatorPort;
import com.aetheri.application.service.token.RefreshTokenService;
import com.aetheri.infrastructure.config.properties.JWTProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring WebFlux 애플리케이션에서 JWT(JSON Web Token) 기반 인증을 처리하는 {@link WebFilter} 구현체입니다.
 *
 * <p>이 필터는 들어오는 요청에서 액세스 토큰을 확인하고 유효성을 검증합니다.</p>
 *
 * <h3>주요 역할:</h3>
 * <ul>
 * <li>요청 헤더에서 **액세스 토큰** 추출 및 유효성 검사</li>
 * <li>유효한 경우, 토큰 정보로 {@link Authentication} 객체를 생성하여 {@link ReactiveSecurityContextHolder}에 저장</li>
 * <li>유효하지 않은 토큰의 경우, 인증 정보 없이 필터 체인을 진행 (Security 설정에서 401 처리)</li>
 * </ul>
 *
 * @see JwtTokenValidatorPort 토큰 유효성 검증 포트
 * @see JwtTokenResolverPort 토큰 내용(ID, 역할) 추출 포트
 * @see JWTProperties JWT 설정 정보
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenValidatorPort jwtTokenValidatorPort;
    private final JwtTokenResolverPort jwtTokenResolverPort;
    private final JWTProperties jwtProperties;

    /**
     * 필터 체인의 핵심 로직을 정의합니다. 요청에서 액세스 토큰을 검증하고 인증 정보를 설정합니다.
     *
     * @param exchange 현재 서버 웹 교환 객체입니다.
     * @param chain    다음 필터 또는 핸들러로의 체인입니다.
     * @return 필터 체인 진행을 나타내는 {@code Mono<Void>}입니다.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String accessToken = resolveToken(exchange.getRequest());

        if (accessToken != null && jwtTokenValidatorPort.validateToken(accessToken)) {
            // 1. 유효한 액세스 토큰: 인증 설정 후 체인 진행
            return authenticateAndContinue(exchange, chain, accessToken);
        }

        // 2. 토큰이 없거나 유효하지 않은 경우 (만료 포함): 인증 정보 없이 체인 진행
        // (이후 Security 설정에서 401 Unauthorized 오류가 발생할 수 있음)
        return chain.filter(exchange);
    }

    /**
     * 토큰이 유효할 때 인증 정보를 설정하고 필터 체인을 계속 진행합니다.
     *
     * @param exchange 현재 서버 웹 교환 객체입니다.
     * @param chain    다음 필터 체인입니다.
     * @param token    유효한 액세스 토큰 문자열입니다.
     * @return 인증 정보를 {@code SecurityContext}에 설정한 후의 {@code Mono<Void>}입니다.
     */
    private Mono<Void> authenticateAndContinue(ServerWebExchange exchange, WebFilterChain chain, String token) {
        Long id = jwtTokenResolverPort.getIdFromToken(token);
        List<GrantedAuthority> authorities = jwtTokenResolverPort.getRolesFromToken(token)
                .stream()
                .filter(role -> role != null && !role.isBlank()) // null, 빈 문자열 제거
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Authentication authentication = generateAuthentication(id, authorities);

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    /**
     * 요청 헤더에서 "Bearer " 스키마를 포함하는 액세스 토큰 문자열을 추출합니다.
     *
     * @param request 서버 요청 객체입니다.
     * @return 추출된 토큰 문자열입니다. 헤더가 없거나 형식이 일치하지 않으면 {@code null}을 반환합니다.
     */
    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(jwtProperties.accessTokenHeader());
        // JWTProperties에 설정된 액세스 토큰 헤더 이름 사용
        if (StringUtils.hasText(bearerToken) && StringUtils.startsWithIgnoreCase(bearerToken, "Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }

    /**
     * 주어진 사용자 ID와 권한 목록을 기반으로 {@link Authentication} 객체를 생성합니다.
     *
     * @param id          토큰에서 추출된 사용자 ID입니다.
     * @param authorities 사용자의 권한 목록입니다.
     * @return 생성된 {@code Authentication} 객체입니다.
     */
    private Authentication generateAuthentication(Long id, List<GrantedAuthority> authorities) {
        // UserDetails의 username 필드에 사용자 ID를 문자열로 저장
        UserDetails userDetails = new User(String.valueOf(id), "", authorities);
        // 자격 증명(credentials)은 사용하지 않으므로 null로 설정
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}