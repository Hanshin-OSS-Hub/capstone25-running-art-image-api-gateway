package com.aetheri.infrastructure.adapter.in.web.router;

import com.aetheri.infrastructure.adapter.in.web.handler.TokenRefreshHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TokenRefreshRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/token",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = TokenRefreshHandler.class,
                    beanMethod = "tokenRefresh",
                    operation = @Operation(
                            operationId = "tokenRefresh",
                            summary = "토큰 재발급을 위한 메소드",
                            tags = {"Token"},
                            responses = {
                                    // 500 응답이 예상되나, 문서화 편의상 200/404를 기본적으로 정의합니다.
                                    @ApiResponse(responseCode = "500", description = "의도된 BusinessException 오류 발생"),
                                    @ApiResponse(responseCode = "404", description = "Not Found")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> tokenRoute(TokenRefreshHandler handler) {
        return route(POST("/api/v1/token"), handler::tokenRefresh);
    }
}