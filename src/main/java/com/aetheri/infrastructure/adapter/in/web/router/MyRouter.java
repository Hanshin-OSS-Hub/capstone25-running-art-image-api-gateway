package com.aetheri.infrastructure.adapter.in.web.router;

import com.aetheri.infrastructure.adapter.in.web.handler.MyHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * 테스트 및 예시용 HTTP 엔드포인트를 정의하는 **WebFlux 라우터 설정 클래스**입니다.
 *
 * <p>함수형 엔드포인트 방식을 사용하여 간단한 {@code /api/hello}와 오류 발생 테스트용
 * {@code /api/hello/error} 경로를 {@code MyHandler}의 메서드에 매핑합니다.</p>
 *
 * <p>{@code @RouterOperations}를 사용하여 SpringDoc(Swagger)을 위한 API 문서화 정보를 제공합니다.</p>
 */
@Configuration
public class MyRouter {


    /**
     * {@code GET /api/hello} 엔드포인트에 대한 라우팅 규칙을 정의하는 {@code RouterFunction} 빈을 생성합니다.
     *
     * <p>해당 요청은 {@code MyHandler#hello} 메서드로 매핑됩니다.</p>
     *
     * @param myHandler "Hello, World!" 응답을 처리하는 핸들러입니다.
     * @return {@code /api/hello} 경로의 라우팅 규칙입니다.
     */
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/hello",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = MyHandler.class,
                    beanMethod = "hello",
                    operation = @Operation(
                            operationId = "hello",
                            summary = "간단한 테스트용 Hello 엔드포인트",
                            tags = {"Hello"},
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "성공적으로 JSON 메시지를 반환합니다.",
                                            content = @Content(schema = @Schema(implementation = String.class))),
                                    @ApiResponse(responseCode = "404", description = "경로를 찾을 수 없습니다.")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> myRoute(MyHandler myHandler) {
        return route(GET("/api/hello"), myHandler::hello);
    }


    /**
     * {@code GET /api/hello/error} 엔드포인트에 대한 라우팅 규칙을 정의하는 {@code RouterFunction} 빈을 생성합니다.
     *
     * <p>해당 요청은 {@code MyHandler#helloError} 메서드로 매핑되며, **의도적으로 예외를 발생**시켜
     * 전역 예외 처리기 테스트에 사용됩니다.</p>
     *
     * @param handler 오류를 발생시키는 핸들러입니다.
     * @return {@code /api/hello/error} 경로의 라우팅 규칙입니다.
     */
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/hello/error",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = MyHandler.class,
                    beanMethod = "helloError",
                    operation = @Operation(
                            operationId = "helloError",
                            summary = "전역 예외 처리를 테스트하기 위한 오류 발생 엔드포인트",
                            tags = {"Hello"},
                            responses = {
                                    // 500 응답이 예상되나, 문서화 편의상 200/404를 기본적으로 정의합니다.
                                    @ApiResponse(responseCode = "500", description = "의도된 BusinessException 오류 발생"),
                                    @ApiResponse(responseCode = "404", description = "Not Found")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> myRouteError(MyHandler handler) {
        return route(GET("/api/hello/error"), handler::helloError);
    }
}