package com.aetheri.infrastructure.adapter.in.web.router;

import com.aetheri.infrastructure.adapter.in.web.handler.ImageFetchHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * **달리기 기록 아트 이미지 파일 조회** 엔드포인트를 정의하는 WebFlux 라우터 설정 클래스입니다.
 *
 * <p>함수형 엔드포인트 방식을 사용하여 특정 경로({@code /api/v1/running-art/{path}})로 들어오는
 * 이미지 요청을 {@code ImageFetchHandler}와 연결합니다.</p>
 *
 * <p>{@code @RouterOperation}을 사용하여 SpringDoc(Swagger)을 위한 상세 API 문서화 정보를 제공합니다.</p>
 */
@Configuration
public class ImageFetchRouter {

    /**
     * 이미지 파일 조회 요청에 대한 라우팅 규칙을 정의하는 {@code RouterFunction} 빈을 생성합니다.
     *
     * <p>경로 변수({@code {path}})를 통해 요청된 이미지 파일을 {@code ImageFetchHandler#imageFetch} 메서드로 매핑합니다.</p>
     *
     * @param imageFetchHandler 이미지 파일 조회를 처리하는 핸들러입니다.
     * @return 정의된 이미지 조회 라우팅 규칙을 포함하는 {@code RouterFunction<ServerResponse>}입니다.
     */
    @Bean
    @RouterOperation(
            path = "/api/v1/running-art/{path}",
            method = RequestMethod.GET,
            beanClass = ImageFetchHandler.class,
            beanMethod = "imageFetch",
            operation = @Operation(
                    operationId = "fetchRunnerArtImage",
                    summary = "경로 변수를 사용하여 달리기 기록 아트 이미지 가져오기",
                    description = "이미지 파일 시스템 경로를 기반으로 이미지를 Resource 형태로 스트리밍합니다. Cache-Control 헤더가 설정됩니다.",
                    tags = {"Image Fetch"},
                    parameters = {
                            @Parameter(
                                    in = ParameterIn.PATH,
                                    name = "path",
                                    description = "이미지 파일명 또는 저장 경로(확장자 포함)",
                                    required = true,
                                    example = "sample_image.png"
                            )
                    },
                    responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "이미지 리소스가 성공적으로 반환됨. Content-Type: image/jpeg",
                                    content = @Content(
                                            mediaType = MediaType.IMAGE_JPEG_VALUE,
                                            // 응답이 파일(바이너리) 데이터임을 스키마로 명시합니다.
                                            schema = @Schema(type = "string", format = "binary")
                                    )
                            ),
                            @ApiResponse(
                                    responseCode = "404",
                                    description = "해당 경로의 이미지를 찾을 수 없음"
                            )
                    }
            )
    )
    public RouterFunction<ServerResponse> imageFetchRoutes(ImageFetchHandler imageFetchHandler) {
        return RouterFunctions.route()
                .GET("/api/v1/running-art/{path}", imageFetchHandler::imageFetch)
                .build();
    }
}