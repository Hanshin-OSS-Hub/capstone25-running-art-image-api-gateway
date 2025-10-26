package com.aetheri.application.port.in.imagemetadata;

import com.aetheri.application.result.imagemetadata.ImageMetadataResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 이미지 메타데이터 조회와 관련된 비즈니스 로직을 정의하는 유즈케이스 인터페이스입니다.
 * 이 인터페이스는 이미지 ID 또는 사용자 ID를 기반으로 메타데이터를 검색하는 기능을 제공합니다.
 */
public interface FindImageMetadataUseCase {
    /**
     * 특정 요청 사용자(소유자)에 대해 이미지 ID로 이미지 메타데이터를 조회합니다.
     *
     * <p>요청자({@code runnerId})가 해당 이미지의 소유자인 경우에만 메타데이터를 반환합니다.
     * 이는 소유권 확인(Ownership Check)을 위한 용도로 사용됩니다.</p>
     *
     * @param runnerId 메타데이터 조회를 요청하는 사용자의 고유 식별자(ID)입니다. (소유권 확인에 사용됨)
     * @param imageId 조회할 이미지 메타데이터의 고유 식별자(ID)입니다.
     * @return 요청자가 이미지 소유자일 경우 {@code ImageMetadataResult}를 발행하는 {@code Mono} 객체입니다.
     */
    Mono<ImageMetadataResult> findImageMetadataById(Long runnerId, Long imageId);

    /**
     * 이미지 ID로 이미지 메타데이터를 조회합니다. (인증 선택 사항)
     *
     * <p>요청자 인증(Authentication) 없이 호출할 수 있으나, 해당 메타데이터가 외부에 공유 또는 공개 설정되지 않은 경우 조회가 허용되지 않습니다.</p>
     *
     * @param imageId 조회할 이미지 메타데이터의 고유 식별자(ID)입니다.
     * @return 이미지 메타데이터를 담은 {@code Mono<ImageMetadataResult>} 객체입니다.
     * @implSpec 요청자 인증 없이 호출 가능하되, 메타데이터의 공개/공유 상태에 따라 조회 가능 여부가 결정됩니다.
     */
    Mono<ImageMetadataResult> findImageMetadataById(Long imageId);

    /**
     * 지정된 사용자가 소유한 모든 이미지 메타데이터를 조회합니다.
     *
     * <p>주어진 사용자 ID({@code runnerId})가 소유한 모든 이미지 메타데이터 DTO({@code ImageMetadataResult}) 스트림을 반환합니다.</p>
     *
     * @param runnerId 이미지 메타데이터를 조회할 사용자(소유자)의 고유 식별자(ID)입니다.
     * @return 해당 사용자(runner)가 소유한 {@code ImageMetadataResult} 객체들을 발행하는 {@code Flux} 객체입니다.
     * @implSpec 결과는 Reactor의 {@code Flux} 형태로 반환되며, 일반적으로 **Server-Sent Events (SSE)**를 통해 클라이언트에게 스트리밍 방식으로 전달되도록 의도됩니다.
     */
    Flux<ImageMetadataResult> findImageMetadataByRunnerId(Long runnerId);
}