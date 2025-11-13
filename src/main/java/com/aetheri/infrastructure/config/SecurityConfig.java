package com.aetheri.infrastructure.config;

import com.aetheri.application.port.in.token.RefreshTokenUseCase;
import com.aetheri.application.port.out.jwt.JwtTokenResolverPort;
import com.aetheri.application.port.out.jwt.JwtTokenValidatorPort;
import com.aetheri.infrastructure.adapter.in.jwt.JwtAuthenticationFilter;
import com.aetheri.infrastructure.config.properties.JWTProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;


/**
 * Spring WebFlux 기반의 **반응형(Reactive) 보안 설정 클래스**입니다.
 *
 * <p>이 클래스는 JWT(JSON Web Token)를 사용하여 세션리스(Stateless) 인증 방식을 구현하며,
 * 특정 경로에 대한 접근 권한 설정과 커스텀 JWT 필터({@link JwtAuthenticationFilter}) 등록을 담당합니다.</p>
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenResolverPort jwtTokenResolverPort;
    private final JwtTokenValidatorPort jwtTokenValidatorPort;
    private final JWTProperties jwtProperties;
    private final RefreshTokenUseCase refreshTokenUseCase;

    /**
     * 애플리케이션의 보안 필터 체인을 구성하는 {@link SecurityWebFilterChain} 빈을 정의합니다.
     *
     * @param http Spring Security 설정을 위한 {@code ServerHttpSecurity} 빌더입니다.
     * @return 설정이 완료된 {@code SecurityWebFilterChain} 인스턴스입니다.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // JWT 기반 인증을 사용하므로 CSRF 보호 기능을 비활성화합니다.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // HTTP Basic 인증을 비활성화합니다.
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                /*
                Spring Security의 요청 캐싱 기능을 비활성화합니다.
                API 서버는 리다이렉션이 필요 없으므로 이 기능을 비활성화하여 오버헤드를 줄입니다.
                */
                .requestCache(ServerHttpSecurity.RequestCacheSpec::disable)

                // 인증에 따른 접근 가능 여부 설정
                .authorizeExchange(exchanges -> exchanges
                        // Spring Actuator 관련 URL은 인증 없이 접근을 허용합니다. (헬스 체크 등)
                        .pathMatchers("/actuator/**").permitAll()
                        // Swagger UI 및 API 문서 관련 URL은 인증 없이 접근을 허용합니다.
                        .pathMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/swagger-ui/**",
                                "/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()
                        // 테스트용 경로 (예: 헬로 월드) 접근 허용
                        .pathMatchers("/api/hello/**").permitAll()
                        // 이미지 스트리밍 전체 조회 (GET /api/v1/image)는 인증된 사용자만 접근 가능
                        .pathMatchers(HttpMethod.GET, "/api/v1/image").authenticated()
                        // 이미지 단건 조회 (GET /api/v1/image/{id})는 인증 없이 접근 허용
                        .pathMatchers(HttpMethod.GET, "/api/v1/image/**").permitAll()
                        // 런닝 아트 관련 조회 API는 인증 없이 접근 허용
                        .pathMatchers(HttpMethod.GET, "/api/v1/running-art/**").permitAll()
                        // 카카오 인증 및 로그인 관련 엔드포인트는 인증 없이 접근 허용
                        .pathMatchers("/api/v1/auth/authorization/kakao", "/api/v1/auth/sign-in", "/login/oauth2/code/kakao").permitAll()
                        // 위에 명시된 경로를 제외한 모든 요청은 인증된 사용자만 접근할 수 있도록 설정합니다.
                        .anyExchange().authenticated()
                )
                // 폼 기반 로그인 기능을 명시적으로 비활성화합니다.
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                // JWT를 사용하는 세션리스 인증이므로, 보안 컨텍스트를 저장하지 않도록 설정합니다.
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // 인증 필터 체인에 커스텀 JWT 인증 필터({@code JwtAuthenticationFilter})를 추가합니다.
                // 이 필터는 HTTP 요청에서 JWT를 추출하고 유효성을 검사하여 인증 객체를 설정하는 역할을 합니다.
                .addFilterAt(
                        new JwtAuthenticationFilter(
                                jwtTokenValidatorPort,
                                jwtTokenResolverPort,
                                jwtProperties
                        ),
                        SecurityWebFiltersOrder.AUTHENTICATION
                )
                .build();
    }
}