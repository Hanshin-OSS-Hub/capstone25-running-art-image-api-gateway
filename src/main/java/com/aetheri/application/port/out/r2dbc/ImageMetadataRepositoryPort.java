package com.aetheri.application.port.out.r2dbc;

import com.aetheri.application.command.imagemetadata.ImageMetadataSaveCommand;
import com.aetheri.application.command.imagemetadata.ImageMetadataUpdateCommand;
import com.aetheri.domain.model.ImageMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 이미지 메타데이터를 데이터베이스에서 조작하기 위한 포트
 */
public interface ImageMetadataRepositoryPort {
    /**
     * 새로운 이미지 메타데이터를 저장합니다.
     * <p>
     * 제공된 DTO에 설명된 메타데이터를 영속화하고 생성된 메타데이터 식별자를 방출합니다.
     *
     * @param runnerId 이미지 메타데이터의 소유자(러너/사용자) 식별자
     * @param dto 이미지 메타데이터를 생성하는 데 필요한 필드를 포함하는 DTO
     * @return 영속화가 완료될 때 생성된 이미지 메타데이터 ID를 방출하는 Mono
     */
    Mono<Long> saveImageMetadata(Long runnerId, ImageMetadataSaveCommand dto);

    /**
     * 이미지 식별자를 통해 이미지 메타데이터를 검색합니다.
     *
     * @param imageId 메타데이터를 검색할 이미지의 고유 식별자
     * @return 발견되면 ImageMetadata를 방출하고, 일치하는 레코드가 없으면 빈 Mono를 방출하는 Mono
     */
    Mono<ImageMetadata> findById(Long imageId);

    /**
     * 주어진 러너(사용자) ID와 관련된 모든 이미지 메타데이터 레코드를 검색합니다.
     *
     * @param runnerId 이미지 메타데이터를 반환해야 하는 러너(사용자) 식별자
     * @return 지정된 러너에 대한 ImageMetadata의 반응형 스트림(Flux); 발견되지 않으면 빈 Flux로 완료됨
     */
    Flux<ImageMetadata> findByRunnerId(Long runnerId);

    /**
     * 지정된 러너와 이미지에 대한 이미지 메타데이터를 업데이트합니다.
     * <p>
     * runnerId가 소유하고 imageId로 식별되는 이미지 메타데이터에 업데이트 요청에 제공된 변경 사항을 적용합니다.
     *
     * @param runnerId 업데이트 범위를 지정하는 데 사용되는 소유자/러너 식별자
     * @param imageId 업데이트할 이미지 메타데이터의 식별자
     * @param request 수정할 필드를 포함하는 업데이트 페이로드
     * @return 업데이트된 레코드 수(일치하는 레코드가 발견되지 않은 경우 0)를 방출하는 Mono
     */
    Mono<Long> updateImageMetadata(Long runnerId, Long imageId, ImageMetadataUpdateCommand request);

    /**
     * 주어진 이미지 ID에 대해 이미지 메타데이터가 존재하는지 확인합니다.
     *
     * @param imageId 메타데이터 존재 여부를 확인할 이미지의 ID
     * @return 이미지 ID에 대한 메타데이터가 존재하면 {@code true}를, 그렇지 않으면 {@code false}를 방출하는 Mono
     */
    Mono<Boolean> isExistImageMetadata(Long imageId);

    /**
     * 주어진 러너(소유자) 및 이미지 ID로 식별되는 이미지 메타데이터를 삭제합니다.
     *
     * <p>이 작업은 지정된 runnerId가 소유한 메타데이터만 제거되도록 해당 러너 ID로 범위가 지정됩니다.</p>
     *
     * @param runnerId 메타데이터의 소유자와 일치해야 하는 소유자/러너 ID
     * @param imageId 삭제할 이미지 메타데이터 식별자
     * @return 결과 지표(일반적으로 삭제된 레코드 수)를 방출하는 Mono
     */
    Mono<Long> deleteById(Long runnerId, Long imageId);

    /**
     * 지정된 러너에게 속한 모든 이미지 메타데이터 레코드를 삭제합니다.
     *
     * @param runnerId 이미지 메타데이터를 삭제해야 하는 러너의 ID
     * @return 삭제된 레코드 수를 방출하는 Mono
     */
    Mono<Long> deleteByRunnerId(Long runnerId);
}