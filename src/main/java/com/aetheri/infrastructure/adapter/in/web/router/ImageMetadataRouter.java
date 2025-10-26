package com.aetheri.infrastructure.adapter.in.web.router;

import com.aetheri.infrastructure.adapter.in.web.dto.in.ImageMetadataSaveRequest;
import com.aetheri.infrastructure.adapter.in.web.dto.in.ImageMetadataUpdateRequest;
import com.aetheri.infrastructure.adapter.in.web.dto.out.ImageMetadataResponse;
import com.aetheri.infrastructure.adapter.in.web.handler.ImageMetadataHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 이미지 메타데이터(제목, 설명 등)에 대한 CRUD API 엔드포인트를 정의하는 **WebFlux 라우터 설정 클래스**입니다.
 *
 * <p>함수형 엔드포인트 방식을 사용하여 {@code /api/v1/image} 경로에 대한 라우팅 규칙을 설정하고,
 * 각 요청을 {@code ImageMetadataHandler}의 메서드에 매핑합니다.
 * 특히, 전체 조회는 **Server-Sent Events (SSE)**를 사용하도록 구성되어 있습니다.</p>
 */
@Slf4j
@Configuration
public class ImageMetadataRouter {
    @Bean
    @RouterOperations({
            // 1. 단건 조회 (GET /api/v1/image/{imageId})
            @RouterOperation(
                    path = "/api/v1/image/{imageId}",
                    method = RequestMethod.GET,
                    beanClass = ImageMetadataHandler.class,
                    beanMethod = "findImage",
                    operation = @Operation(
                            operationId = "findImageById",
                            summary = "ID로 이미지 메타데이터를 조회합니다.",
                            description = "인증된 사용자는 자신의 이미지를 조회할 수 있으며, 공유된(Shared) 이미지도 조회할 수 있습니다.",
                            tags = {"Image Metadata"},
                            parameters = {
                                    @Parameter(
                                            name = "imageId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "조회할 이미지의 고유 ID"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "정상적으로 이미지 메타데이터를 반환합니다.",
                                            content = @Content(schema = @Schema(implementation = ImageMetadataResponse.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "해당 ID의 이미지를 찾을 수 없습니다."
                                    )
                            }
                    )
            ),
            // 2. 전체 조회 (GET /api/v1/image) - SSE 사용
            @RouterOperation(
                    path = "/api/v1/image",
                    method = RequestMethod.GET,
                    beanClass = ImageMetadataHandler.class,
                    beanMethod = "findImagesByRunnerId",
                    operation = @Operation(
                            operationId = "findImagesByRunnerId",
                            summary = "인증된 사용자의 모든 이미지 메타데이터를 SSE로 스트리밍 조회합니다.",
                            description = "인증된 사용자만 접근 가능하며, 데이터는 Server-Sent Events(text/event-stream) 형식으로 스트리밍됩니다.",
                            tags = {"Image Metadata"},
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "이미지 메타데이터 목록이 SSE 형태로 스트리밍됩니다.",
                                            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(implementation = ImageMetadataResponse.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "인증 정보가 유효하지 않습니다."
                                    )
                            }
                    )
            ),
            // 3. 삭제 (DELETE /api/v1/image/{imageId})
            @RouterOperation(
                    path = "/api/v1/image/{imageId}",
                    method = RequestMethod.DELETE,
                    beanClass = ImageMetadataHandler.class,
                    beanMethod = "deleteImage",
                    operation = @Operation(
                            operationId = "deleteImageById",
                            summary = "ID로 이미지 메타데이터를 삭제합니다. (소유자만 가능)",
                            tags = {"Image Metadata"},
                            parameters = {
                                    @Parameter(
                                            name = "imageId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "삭제할 이미지의 고유 ID"
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "성공적으로 이미지를 삭제했습니다."
                                    )
                            }
                    )
            ),
            // 4. 수정 (PUT /api/v1/image/{imageId})
            @RouterOperation(
                    path = "/api/v1/image/{imageId}",
                    method = RequestMethod.PUT,
                    beanClass = ImageMetadataHandler.class,
                    beanMethod = "updateImage",
                    operation = @Operation(
                            operationId = "updateImageById",
                            summary = "ID로 이미지 메타데이터를 업데이트합니다. (소유자만 가능)",
                            tags = {"Image Metadata"},
                            parameters = {
                                    @Parameter(
                                            name = "imageId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "업데이트할 이미지의 고유 ID"
                                    )
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "업데이트할 이미지 메타데이터",
                                    content = @Content(
                                            schema = @Schema(implementation = ImageMetadataUpdateRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "성공적으로 이미지 메타데이터를 업데이트했습니다."
                                    )
                            }
                    )
            ),
            // 5. 저장 (POST /api/v1/image)
            @RouterOperation(
                    path = "/api/v1/image",
                    method = RequestMethod.POST,
                    beanClass = ImageMetadataHandler.class,
                    beanMethod = "saveImage",
                    operation = @Operation(
                            operationId = "saveImage",
                            summary = "새로운 이미지 메타데이터를 저장합니다.",
                            tags = {"Image Metadata"},
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "저장할 이미지 메타데이터",
                                    content = @Content(
                                            schema = @Schema(implementation = ImageMetadataSaveRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "성공적으로 이미지를 저장했습니다."
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> imageRoutes(ImageMetadataHandler handler) {
        return RouterFunctions.route()
                .path("/api/v1/image", builder -> builder
                        // POST /api/v1/image: 저장
                        .POST("", handler::saveImage)
                        // GET /api/v1/image: 전체 조회 (SSE)
                        .GET("", request -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_EVENT_STREAM) // SSE Content-Type 설정
                                .body(handler.findImagesByRunnerId(request), ServerSentEvent.class)
                        )
                        // GET /api/v1/image/{imageId}: 단건 조회
                        .GET("/{imageId}", handler::findImage)
                        // DELETE /api/v1/image/{imageId}: 삭제
                        .DELETE("/{imageId}", handler::deleteImage)
                        // PUT /api/v1/image/{imageId}: 수정
                        .PUT("/{imageId}", handler::updateImage)
                )
                .build();
    }
}