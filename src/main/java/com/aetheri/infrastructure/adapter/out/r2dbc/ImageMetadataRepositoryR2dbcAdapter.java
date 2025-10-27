package com.aetheri.infrastructure.adapter.out.r2dbc;

import com.aetheri.application.command.imagemetadata.ImageMetadataSaveCommand;
import com.aetheri.application.command.imagemetadata.ImageMetadataUpdateCommand;
import com.aetheri.application.port.out.r2dbc.ImageMetadataRepositoryPort;
import com.aetheri.application.port.out.r2dbc.ImageMetadataR2dbcRepository;
import com.aetheri.domain.model.ImageMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 이미지 메타데이터 관리 포트({@link ImageMetadataRepositoryPort})의 R2DBC 기반 구현체입니다.
 * 이 어댑터는 {@code ImageMetadata} 엔티티에 대한 CRUD 작업을 수행하며,
 * 복잡한 쿼리에는 {@link R2dbcEntityTemplate}를 사용하고 단순 작업에는 {@link ImageMetadataR2dbcRepository}를 사용합니다.
 *
 * <p>모든 데이터베이스 작업은 비동기/논블로킹 방식으로 {@code Mono} 또는 {@code Flux}를 반환합니다.</p>
 */
@Repository
@RequiredArgsConstructor
public class ImageMetadataRepositoryR2dbcAdapter implements ImageMetadataRepositoryPort {
    private final ImageMetadataR2dbcRepository imageMetadataR2DbcRepository;
    /**
     * 새로운 이미지의 메타데이터를 데이터베이스에 저장합니다.
     *
     * @param runnerId 이미지를 등록하는 사용자의 고유 ID입니다.
     * @param request  등록할 이미지의 메타데이터를 가진 DTO입니다.
     * @return 생성이 성공하면 저장된 이미지 메타데이터의 고유 ID({@code Long})를 발행하는 {@code Mono}입니다.
     */
    public Mono<Long> saveImageMetadata(Long runnerId, ImageMetadataSaveCommand request) {
        // 엔티티 생성 시, 고유한 fileKey를 생성하여 사용합니다.
        ImageMetadata entity = ImageMetadata.toEntity(
                runnerId,
                runnerId + "-" + UUID.randomUUID(),
                request.location(),
                request.shape(),
                request.proficiency()
        );
        return imageMetadataR2DbcRepository.save(entity)
                .flatMap(this::getImageMetadataId);
    }

    /**
     * 이미지의 고유 ID({@code imageId})를 사용하여 데이터베이스에서 이미지의 메타데이터를 조회합니다.
     *
     * @param imageId 조회할 이미지의 고유 ID입니다.
     * @return 조회된 {@code ImageMetadata} 엔티티를 발행하는 {@code Mono}입니다.
     */
    public Mono<ImageMetadata> findById(Long imageId) {
        return imageMetadataR2DbcRepository.findById(imageId);
    }


    /**
     * 특정 사용자 ID({@code runnerId})에 의해 등록된 모든 이미지의 메타데이터 목록을 조회합니다.
     *
     * @param runnerId 이미지를 조회할 사용자의 고유 ID입니다.
     * @return 해당 사용자의 모든 {@code ImageMetadata} 엔티티를 발행하는 {@code Flux}입니다.
     */
    public Flux<ImageMetadata> findByRunnerId(Long runnerId) {
        return imageMetadataR2DbcRepository.findAllByRunnerId(runnerId);
    }


    /**
     * 주어진 이미지 ID에 해당하는 이미지 메타데이터를 업데이트합니다.
     *
     * @param runnerId 수정을 요청한 사용자의 ID (소유자 검증용)
     * @param imageId  수정할 이미지의 고유 ID입니다.
     * @param request  업데이트할 내용을 담은 DTO입니다.
     * @return 업데이트된 행의 개수({@code Long})를 발행하는 {@code Mono}입니다.
     * @implNote **소유자({@code runnerId})와 이미지 ID({@code imageId})**를 모두 사용하여 쿼리하여, 해당 이미지의 소유자만 수정할 수 있도록 보장합니다.
     */
    public Mono<Long> updateImageMetadata(Long runnerId, Long imageId, ImageMetadataUpdateCommand request) {
        return imageMetadataR2DbcRepository.findById(imageId) // 1. 포트를 통해 조회
                .flatMap(imageMetadata -> {
                    // 2. 도메인 규칙 검사
                    if (!imageMetadata.getRunnerId().equals(runnerId)) {
                        // 소유자가 아님
                        return Mono.just(0L);
                    }

                    // 3. 도메인 로직 실행 (상태 변경)
                    imageMetadata.update(request.title(), request.description());

                    // 4. 'save'가 반환하는 Mono를 리턴 체인에 포함시킴
                    return imageMetadataR2DbcRepository.save(imageMetadata)
                            .map(savedEntity -> 1L); // 5. 저장이 완료되면 그 결과를 1L로 변환하여 반환
                })
                .defaultIfEmpty(0L); // 6. findById로 찾지 못했을 경우 0L 반환
    }

    /**
     * 주어진 이미지 ID를 가진 메타데이터가 데이터베이스에 존재하는지 확인합니다.
     *
     * @param imageId 존재 여부를 확인할 이미지의 고유 ID입니다.
     * @return 이미지가 존재하면 {@code true}를, 아니면 {@code false}를 발행하는 {@code Mono<Boolean>}입니다.
     */
    public Mono<Boolean> isExistImageMetadata(Long imageId) {
        return imageMetadataR2DbcRepository.existsById(imageId);
    }

    /**
     * 주어진 이미지 ID에 해당하는 이미지 메타데이터를 삭제합니다.
     *
     * @param runnerId 삭제를 요청한 사용자의 ID (소유자 검증용)
     * @param imageId  삭제할 이미지의 고유 ID입니다.
     * @return 삭제된 행의 개수({@code Long})를 발행하는 {@code Mono}입니다.
     * @implNote **소유자({@code runnerId})와 이미지 ID({@code imageId})**를 모두 사용하여 쿼리하여, 해당 이미지의 소유자만 삭제할 수 있도록 보장합니다.
     */
    public Mono<Long> deleteById(Long runnerId, Long imageId) {
        return imageMetadataR2DbcRepository.findById(imageId) // 1. 포트를 통해 조회
                .flatMap(imageMetadata -> {
                    // 2. 도메인 규칙 검사
                    if (!imageMetadata.getRunnerId().equals(runnerId)) {
                        // 소유자가 아님
                        return Mono.just(0L);
                    }

                    // 3. 'delete'를 리턴 체인에 포함시킵니다.
                    // .delete()는 Mono<Void>를 반환합니다.
                    return imageMetadataR2DbcRepository.delete(imageMetadata)
                            .then(Mono.just(1L)); // 4. 삭제가 완료된 '후에' 1L을 반환합니다.
                })
                .defaultIfEmpty(0L); // 6. findById로 찾지 못했을 경우 0L 반환
    }

    /**
     * 특정 사용자 ID({@code runnerId})에 의해 등록된 **모든 이미지 메타데이터**를 삭제합니다.
     *
     * <p>주로 회원 탈퇴와 같은 전역적인 정리 작업에 사용됩니다.</p>
     *
     * @param runnerId 이미지를 삭제할 사용자의 고유 ID입니다.
     * @return 삭제된 행의 개수({@code Long})를 발행하는 {@code Mono}입니다.
     */
    public Mono<Long> deleteByRunnerId(Long runnerId) {
        return imageMetadataR2DbcRepository.deleteAllByRunnerId(runnerId);
    }

    /**
     * 저장 작업 후 반환된 {@code ImageMetadata} 엔티티에서 고유 ID(PK)를 추출합니다.
     *
     * @param imageMetadata 저장된 {@code ImageMetadata} 엔티티입니다.
     * @return 엔티티의 ID를 발행하는 {@code Mono<Long>}입니다.
     */
    private Mono<Long> getImageMetadataId(ImageMetadata imageMetadata) {
        return Mono.just(imageMetadata.getId());
    }
}