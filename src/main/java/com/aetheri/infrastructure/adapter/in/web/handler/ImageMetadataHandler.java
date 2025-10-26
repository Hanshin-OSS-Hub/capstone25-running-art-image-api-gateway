package com.aetheri.infrastructure.adapter.in.web.handler;


import com.aetheri.application.port.in.imagemetadata.DeleteImageMetadataUseCase;
import com.aetheri.application.port.in.imagemetadata.FindImageMetadataUseCase;
import com.aetheri.application.port.in.imagemetadata.SaveImageMetadataUseCase;
import com.aetheri.application.port.in.imagemetadata.UpdateImageMetadataUseCase;
import com.aetheri.infrastructure.util.AuthenticationUtils;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.infrastructure.adapter.in.web.dto.in.ImageMetadataSaveRequest;
import com.aetheri.infrastructure.adapter.in.web.dto.in.ImageMetadataUpdateRequest;
import com.aetheri.infrastructure.adapter.in.web.dto.out.ImageMetadataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * 이미지 메타데이터(제목, 설명, 위치 등) 조회, 저장, 수정, 삭제 요청을 처리하는 **WebFlux 핸들러**입니다.
 *
 * <p>주로 {@code ServerRequest}에서 인증 정보를 추출하여 {@code UseCase}에 전달하고,
 * 그 결과를 {@code ServerResponse}로 변환하여 클라이언트에게 응답합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageMetadataHandler {
    private final FindImageMetadataUseCase findImageMetadataUseCase;
    private final DeleteImageMetadataUseCase deleteImageMetadataUseCase;
    private final SaveImageMetadataUseCase saveImageMetadataUseCase;
    private final UpdateImageMetadataUseCase updateImageMetadataUseCase;
    // Server-Sent Events (SSE) 응답을 위한 인코더 및 버퍼 설정
    private final Jackson2JsonEncoder jackson2JsonEncoder;
    private final DefaultDataBufferFactory dataBufferFactory;
    private final ResolvableType elementType = ResolvableType.forClass(ImageMetadataResponse.class);


    /**
     * 특정 이미지 ID에 해당하는 메타데이터를 조회합니다.
     *
     * <p>요청한 사용자의 ID를 기반으로 해당 이미지가 **사용자 소유이거나 공유 가능한 이미지**인 경우에만 조회합니다.</p>
     *
     * @param request 경로 변수({@code imageId})를 포함하는 서버 요청 정보입니다.
     * @return {@code ImageMetadataResponse}를 본문으로 담는 {@code Mono<ServerResponse>}입니다.
     */
    public Mono<ServerResponse> findImage(ServerRequest request) {
        Long imageId = Long.parseLong(request.pathVariable("imageId"));
        // 1. 인증된 사용자 ID로 조회 (사용자 소유 여부 확인)
        return AuthenticationUtils.extractRunnerIdFromRequest(request)
                .flatMap(runnerId -> findImageMetadataUseCase.findImageMetadataById(runnerId, imageId))
                // 2. 만약 사용자 소유가 아닌 경우, 공개된 이미지인지 확인 (공개된 이미지만 조회)
                .switchIfEmpty(findImageMetadataUseCase.findImageMetadataById(imageId))
                .map(ImageMetadataResponse::toResponse)
                .flatMap(image -> ServerResponse.ok().bodyValue(image));
    }

    /**
     * **인증된 사용자 ID**가 생성한 모든 이미지 메타데이터 목록을 **Server-Sent Events (SSE)** 형태로 스트리밍합니다.
     *
     * <p>데이터베이스에서 발견된 각 이미지는 개별 {@code ServerSentEvent}로 인코딩되어 실시간으로 전송됩니다.</p>
     *
     * @param request 현재 서버 요청 정보입니다.
     * @return {@code DataBuffer}를 포함하는 {@code ServerSentEvent}의 {@code Flux}입니다.
     */
    public Flux<ServerSentEvent<DataBuffer>> findImagesByRunnerId(ServerRequest request) {
        // 1. 인증된 사용자 ID 추출 및 해당 ID의 이미지 메타데이터를 Flux로 조회
        return AuthenticationUtils.extractRunnerIdFromRequest(request)
                .flatMapMany(findImageMetadataUseCase::findImageMetadataByRunnerId)
                .map(ImageMetadataResponse::toResponse)
                // 2. 각 ImageMetadataResponse 객체를 DataBuffer (JSON)로 인코딩
                .flatMap(response -> jackson2JsonEncoder.encode(
                                Flux.just(response),
                                dataBufferFactory,
                                elementType,
                                MediaType.APPLICATION_JSON,
                                Collections.emptyMap()
                        ).cast(DataBuffer.class)
                )
                // 3. 인코딩된 DataBuffer를 SSE 포맷으로 래핑
                .map(dataBuffer -> ServerSentEvent.<DataBuffer>builder()
                        .data(dataBuffer)
                        .build());

    }

    /**
     * 새로운 이미지 메타데이터를 저장합니다.
     *
     * <p>요청 본문({@code ImageMetadataSaveRequest})을 파싱하여 인증된 사용자 ID와 함께
     * {@code SaveImageMetadataUseCase}를 실행합니다.</p>
     *
     * @param request 저장할 메타데이터 DTO를 포함하는 서버 요청 정보입니다.
     * @return 저장 완료 시 200 OK 응답을 반환하는 {@code Mono<ServerResponse>}입니다.
     */
    public Mono<ServerResponse> saveImage(ServerRequest request) {
        return AuthenticationUtils.extractRunnerIdFromRequest(request)
                // 인증 정보가 없으면 예외 발생
                .switchIfEmpty(Mono.error(new BusinessException(ErrorMessage.FORBIDDEN, "유효한 인증 없이 접근할 수 없습니다.")))
                .flatMap(requestId ->
                        request.bodyToMono(ImageMetadataSaveRequest.class)
                                .flatMap(body -> saveImageMetadataUseCase.saveImageMetadata(requestId, body.toCommand()))
                )
                .then(ServerResponse.ok().build());
    }

    /**
     * 특정 이미지 ID에 해당하는 메타데이터를 삭제합니다.
     *
     * <p>인증된 사용자 ID와 이미지 ID를 사용하여 **소유자 검증 후** 삭제를 수행합니다.</p>
     *
     * @param request 경로 변수({@code imageId})를 포함하는 서버 요청 정보입니다.
     * @return 삭제 완료 시 200 OK 응답을 반환하는 {@code Mono<ServerResponse>}입니다.
     */
    public Mono<ServerResponse> deleteImage(ServerRequest request) {
        Long imageId = Long.parseLong(request.pathVariable("imageId"));
        return AuthenticationUtils.extractRunnerIdFromRequest(request)
                // 인증 정보가 없으면 예외 발생
                .switchIfEmpty(Mono.error(new BusinessException(ErrorMessage.FORBIDDEN, "유효한 인증 없이 접근할 수 없습니다.")))
                .flatMap(runnerId -> deleteImageMetadataUseCase.deleteImageMetadata(runnerId, imageId))
                .then(ServerResponse.ok().build());
    }

    /**
     * 특정 이미지 ID에 해당하는 메타데이터를 수정합니다.
     *
     * <p>인증된 사용자 ID, 이미지 ID, 요청 본문({@code ImageMetadataUpdateRequest})을 사용하여
     * **소유자 검증 후** 수정을 수행합니다.</p>
     *
     * @param request 경로 변수({@code imageId})와 수정할 메타데이터 DTO를 포함하는 서버 요청 정보입니다.
     * @return 수정 완료 시 200 OK 응답을 반환하는 {@code Mono<ServerResponse>}입니다.
     */
    public Mono<ServerResponse> updateImage(ServerRequest request) {
        Long imageId = Long.parseLong(request.pathVariable("imageId"));
        return AuthenticationUtils.extractRunnerIdFromRequest(request)
                // 인증 정보가 없으면 예외 발생
                .switchIfEmpty(Mono.error(new BusinessException(ErrorMessage.FORBIDDEN, "유효한 인증 없이 접근할 수 없습니다.")))
                .flatMap(runnerId ->
                        request.bodyToMono(ImageMetadataUpdateRequest.class)
                                .flatMap(body -> updateImageMetadataUseCase.updateImageMetadata(runnerId, imageId, body.toCommand()))
                )
                .then(ServerResponse.ok().build());
    }
}