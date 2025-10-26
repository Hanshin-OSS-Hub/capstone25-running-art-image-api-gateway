package com.aetheri.application.port.in.imagemetadata;

import com.aetheri.application.command.imagemetadata.ImageMetadataSaveCommand;
import reactor.core.publisher.Mono;

/**
 * 이미지 메타데이터 생성(저장)과 관련된 비즈니스 로직을 정의하는 유즈케이스 인터페이스입니다.
 * 이 인터페이스는 특정 사용자를 대신하여 새로운 이미지 메타데이터를 영속화하는 기능을 제공합니다.
 */
public interface SaveImageMetadataUseCase {
    /**
     * 주어진 사용자를 대신하여 이미지 메타데이터를 저장합니다.
     *
     * <p>요청 객체({@code request})에 포함된 이미지 메타데이터를 영속화하고, 이를 제공된 사용자 식별자({@code runnerId})와 연결합니다.</p>
     *
     * @param runnerId 메타데이터를 연결하고 저장할 사용자(Runner)의 고유 식별자(ID)입니다.
     * @param request 저장할 이미지 메타데이터가 담긴 요청 페이로드 DTO입니다.
     * @return 메타데이터 저장 작업이 완료(영속화)되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    Mono<Void> saveImageMetadata(Long runnerId, ImageMetadataSaveCommand request);
}