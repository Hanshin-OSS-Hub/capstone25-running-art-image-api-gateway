package com.aetheri.application.service.imagemetadata;

import com.aetheri.application.result.imagemetadata.ImageMetadataResult;
import com.aetheri.application.port.in.imagemetadata.FindImageMetadataUseCase;
import com.aetheri.application.port.out.r2dbc.ImageMetadataRepositoryPort;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.domain.model.ImageMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 이미지 메타데이터 조회 유즈케이스({@link FindImageMetadataUseCase})를 구현하는 서비스 클래스입니다.
 * 이 클래스는 이미지 ID 또는 사용자 ID를 기반으로 메타데이터를 검색하고, 접근 권한을 확인하는 비즈니스 로직을 수행합니다.
 *
 * @see FindImageMetadataUseCase 구현하는 유즈케이스 인터페이스
 * @see ImageMetadataRepositoryPort 데이터베이스 접근을 위한 아웃고잉 포트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FindImageMetadataService implements FindImageMetadataUseCase {
    private final ImageMetadataRepositoryPort imageMetadataRepositoryPort;

    /**
     * 요청 사용자({@code runnerId})가 소유하거나 접근 권한이 있는 이미지 메타데이터를 ID로 조회합니다.
     *
     * <p>이미지 메타데이터가 존재하고, 해당 이미지가 공유({@code shared})되었거나
     * 요청 사용자({@code runnerId})가 이미지의 소유자인 경우에만 메타데이터를 반환합니다.
     * 메타데이터를 찾을 수 없거나 접근 권한이 없는 경우 {@link BusinessException}을 발생시켜 오류를 알립니다.</p>
     *
     * @param runnerId 메타데이터 조회를 요청하는 사용자의 고유 식별자(ID)입니다. (접근 및 소유권 확인에 사용됨)
     * @param imageId 조회할 이미지 메타데이터의 고유 식별자(ID)입니다.
     * @return 권한 확인 후 메타데이터가 존재하면 {@code ImageMetadataResponse}를 발행하는 {@code Mono} 객체입니다.
     * @throws BusinessException 메타데이터를 찾을 수 없거나(NOT\_FOUND\_IMAGE\_METADATA)
     * 요청자에게 접근 권한이 없을 때(RUNNER\_IS\_NOT\_OWNER\_OF\_IMAGE\_METADATA) 발생합니다.
     */
    @Override
    public Mono<ImageMetadata> findImageMetadataById(Long runnerId, Long imageId) {
        return imageMetadataRepositoryPort.findById(imageId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorMessage.NOT_FOUND_IMAGE_METADATA, "이미지를 찾을 수 없습니다.")))
                .flatMap(imageMetadata -> {
                    log.info("[FindImageMetadataMetadataService] 사용자 {}가 이미지 {}를 조회했습니다.", runnerId, imageId);
                    if (imageMetadata.getShared() || imageMetadata.getRunnerId().equals(runnerId)) {
                        return Mono.just(imageMetadata);
                    } else {
                        return Mono.error(
                                new BusinessException(
                                        ErrorMessage.RUNNER_IS_NOT_OWNER_OF_IMAGE_METADATA,
                                        "요청자는 이미지의 주인이 아닙니다."
                                )
                        );
                    }
                });
    }

    /**
     * 요청 사용자 ID 없이, 이미지 ID만으로 이미지 메타데이터를 조회합니다. (공개 조회)
     *
     * <p>이미지 메타데이터가 존재하고, 해당 이미지가 **공유({@code shared})로 설정된 경우에만** 메타데이터를 반환합니다.
     * 이미지를 찾을 수 없거나 공유되지 않은 경우 {@link BusinessException}을 발생시켜 오류를 알립니다.</p>
     *
     * @param imageId 조회할 이미지 메타데이터의 고유 식별자(ID)입니다.
     * @return 이미지 메타데이터가 존재하고 공유된 경우 {@code ImageMetadataResponse}를 발행하는 {@code Mono} 객체입니다.
     * @throws BusinessException 메타데이터를 찾을 수 없거나(NOT\_FOUND\_IMAGE\_METADATA)
     * 이미지가 존재하지만 공유되지 않았을 때(RUNNER\_IS\_NOT\_OWNER\_OF\_IMAGE\_METADATA) 발생합니다.
     */
    @Override
    public Mono<ImageMetadata> findImageMetadataById(Long imageId) {
        return imageMetadataRepositoryPort.findById(imageId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorMessage.NOT_FOUND_IMAGE_METADATA, "이미지를 찾을 수 없습니다.")))
                .flatMap(imageMetadata -> {
                    log.info("[FindImageMetadataMetadataService] 이미지 {}를 조회했습니다.", imageId);
                    if (imageMetadata.getShared()) {
                        return Mono.just(imageMetadata);
                    } else {
                        return Mono.error(
                                new BusinessException(
                                        ErrorMessage.RUNNER_IS_NOT_OWNER_OF_IMAGE_METADATA,
                                        "요청자는 이미지의 주인이 아닙니다."
                                )
                        );
                    }
                });
    }

    /**
     * 지정된 사용자({@code runnerId})가 소유한 모든 이미지 메타데이터를 조회합니다.
     *
     * @param runnerId 이미지 메타데이터를 조회할 사용자(소유자)의 고유 식별자(ID)입니다.
     * @return 해당 사용자가 소유한 {@code ImageMetadataResponse} 객체들을 연속적으로 발행하는 {@code Flux} 스트림입니다.
     */
    @Override
    public Flux<ImageMetadata> findImageMetadataByRunnerId(Long runnerId) {
        return imageMetadataRepositoryPort.findByRunnerId(runnerId)
                .doOnComplete(() -> log.info("[FindImageMetadataMetadataService] 사용자 {}의 이미지를 조회했습니다.", runnerId));
    }
}