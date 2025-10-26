package com.aetheri.application.port.in.imagemetadata;

import com.aetheri.application.command.imagemetadata.ImageMetadataUpdateCommand;
import reactor.core.publisher.Mono;

/**
 * 이미지 메타데이터 수정과 관련된 비즈니스 로직을 정의하는 유즈케이스 인터페이스입니다.
 * 이 인터페이스는 특정 사용자를 대신하여 기존 이미지 메타데이터를 업데이트하는 기능을 제공합니다.
 */
public interface UpdateImageMetadataUseCase {
    /**
     * 기존 이미지 메타데이터의 내용을 수정합니다.
     *
     * <p>요청하는 사용자({@code runnerId})가 해당 메타데이터의 소유자일 경우에만,
     * {@code imageId}로 식별되는 이미지 메타데이터에 {@code request}에 제공된 변경 사항을 적용합니다.</p>
     *
     * @param runnerId 업데이트를 요청하는 사용자의 고유 식별자(ID)입니다. 업데이트가 적용되려면 이 ID가 이미지 메타데이터의 소유자와 일치해야 합니다.
     * @param imageId 수정할 이미지 메타데이터의 고유 식별자(ID)입니다.
     * @param request 업데이트할 메타데이터 필드(예: 제목, 설명)가 포함된 DTO입니다.
     * @return 업데이트가 성공적으로 적용되었을 때 완료되는 (값을 발행하지 않는) {@code Mono<Void>} 객체입니다.
     */
    Mono<Void> updateImageMetadata(Long runnerId, Long imageId, ImageMetadataUpdateCommand request);
}