package com.aetheri.infrastructure.adapter.in.web.handler;

import com.aetheri.application.port.in.imgfetch.ImageFetchUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * 이미지 파일 조회와 관련된 **HTTP 요청을 처리하는 핸들러 클래스**입니다.
 *
 * <p>요청된 이미지 경로를 기반으로 이미지를 로드하고, 로드된 이미지를
 * {@code Resource} 형태로 클라이언트에게 스트리밍하여 응답합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageFetchHandler {
    private final ImageFetchUseCase imageFetchUseCase;

    /**
     * 특정 경로에 저장된 **이미지 파일({@code Resource})을 조회하고 클라이언트에게 응답**합니다.
     *
     * <p>경로 변수({@code path})에서 이미지 경로를 추출하여 {@code ImageFetchUseCase}에 전달합니다.
     * 응답 시에는 {@code MediaType.IMAGE_JPEG}와 1시간 캐시 설정을 적용합니다.</p>
     *
     * @param serverRequest 경로 변수({@code path})를 포함하는 서버 요청 정보입니다.
     * @return 이미지 리소스({@code Mono<Resource>})를 본문으로 담는 {@code ServerResponse}입니다.
     */
    public Mono<ServerResponse> imageFetch(ServerRequest serverRequest) {
        // 1. 요청 경로 변수에서 이미지 경로 추출
        String path = serverRequest.pathVariable("path");
        // 2. UseCase를 통해 이미지 리소스 비동기 조회
        Mono<Resource> imageMono = imageFetchUseCase.fetchImage(path);

        return ServerResponse.ok()
                // 응답 Content-Type을 JPEG 이미지로 설정합니다.
                // TODO: 추후 이미지 포맷에 따라 MediaType을 동적으로 변경하도록 개선 필요
                .contentType(MediaType.IMAGE_JPEG)
                // 클라이언트와 프록시가 1시간(3600초) 동안 이미지를 캐시할 수 있도록 HTTP 헤더를 설정합니다.
                .header("Cache-Control", "public, max-age=3600")
                // Mono<Resource>를 응답 본문으로 스트리밍합니다.
                .body(imageMono, Resource.class);
    }
}