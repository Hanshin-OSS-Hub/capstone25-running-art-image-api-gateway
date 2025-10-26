package com.aetheri.infrastructure.adapter.in.web.router;


import com.aetheri.infrastructure.adapter.in.web.handler.AuthHandler;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


/**
 * 카카오 인증 및 사용자 관련 API 엔드포인트를 정의하는 **WebFlux 라우터 설정 클래스**입니다.
 *
 * <p>함수형 엔드포인트 방식을 사용하여 {@code /api/v1/auth} 경로에 대한 라우팅 규칙을 설정하고,
 * 각 요청을 {@code AuthHandler}의 메서드에 매핑합니다.</p>
 *
 * <p>{@code @RouterOperations}를 사용하여 SpringDoc(Swagger)을 위한 API 문서화 정보를 제공합니다.</p>
 */
@Slf4j
@Configuration
public class AuthRouter {

    /**
     * 인증 관련 라우팅 규칙을 정의하는 {@code RouterFunction} 빈을 생성합니다.
     *
     * <p>다음 엔드포인트를 포함합니다:</p>
     * <ul>
     * <li>{@code GET /api/v1/auth/authorization/kakao}: 카카오 로그인 페이지로 리다이렉트</li>
     * <li>{@code GET /api/v1/auth/sign-in}: 카카오 인가 코드로 로그인 처리 및 토큰 발급</li>
     * <li>{@code POST /api/v1/auth/sign-off}: 카카오 계정 연결 끊기 및 회원 탈퇴</li>
     * <li>{@code POST /api/v1/auth/sign-out}: 서비스 로그아웃</li>
     * </ul>
     *
     * @param authHandler 인증 요청을 처리하는 핸들러입니다.
     * @return 정의된 라우팅 규칙을 포함하는 {@code RouterFunction<ServerResponse>}입니다.
     */
    @Bean
    @RouterOperations({
            // 1. 카카오 로그인 페이지 리다이렉트
            @RouterOperation(
                    path = "/api/v1/auth/authorization/kakao",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = AuthHandler.class,
                    beanMethod = "redirectToKakaoLogin",
                    operation = @Operation(
                            operationId = "authRedirectToKakao",
                            summary = "카카오 로그인 페이지로 리다이렉트",
                            tags = {"Auth"}
                    )
            ),
            // 2. 로그인 및 토큰 발급
            @RouterOperation(
                    path = "/api/v1/auth/sign-in",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = AuthHandler.class,
                    beanMethod = "getKakaoAccessToken",
                    operation = @Operation(
                            operationId = "authSignIn",
                            summary = "로그인 코드로 카카오 액세스 토큰 발급",
                            tags = {"Auth"}
                    )
            ),
            // 3. 카카오 회원 탈퇴 (연결 끊기)
            @RouterOperation(
                    path = "/api/v1/auth/sign-off",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "signOff",
                    operation = @Operation(
                            operationId = "authSignOff",
                            summary = "카카오 계정 연결 끊기 및 서비스 회원 탈퇴",
                            tags = {"Auth"}
                    )
            ),
            // 4. 서비스 로그아웃
            @RouterOperation(
                    path = "/api/v1/auth/sign-out",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "signOut",
                    operation = @Operation(
                            operationId = "authSignOut",
                            summary = "JWT 토큰 무효화를 통한 서비스 로그아웃",
                            tags = {"Auth"}
                    )
            )
    })
    public RouterFunction<ServerResponse> authRoutes(AuthHandler authHandler) {
        return RouterFunctions.route()
                .path("/api/v1/auth", builder -> builder
                        // 카카오 인가 코드 요청을 위한 리다이렉트 URL
                        .GET("/authorization/kakao", authHandler::redirectToKakaoLogin)
                        // 카카오로부터 인가 코드를 받아 액세스 토큰 발급 및 로그인 처리
                        .GET("/sign-in", authHandler::getKakaoAccessToken)
                        // 회원 탈퇴 (카카오 연결 끊기 포함)
                        .POST("/sign-off", authHandler::signOff)
                        // 서비스 로그아웃 (토큰 무효화)
                        .POST("/sign-out", authHandler::signOut))
                .build();
    }
}