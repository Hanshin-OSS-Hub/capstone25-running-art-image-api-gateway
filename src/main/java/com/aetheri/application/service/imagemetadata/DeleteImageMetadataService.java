package com.aetheri.application.service.imagemetadata;

import com.aetheri.application.port.in.imagemetadata.DeleteImageMetadataUseCase;
import com.aetheri.application.port.out.imagemetadata.ImageMetadataRepositoryPort;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 이미지 메타데이터 삭제 유즈케이스({@link DeleteImageMetadataUseCase})를 구현하는 서비스 클래스입니다.
 * 이 클래스는 특정 사용자의 요청에 따라 이미지 메타데이터를 데이터베이스에서 삭제하는 비즈니스 로직을 수행합니다.
 *
 * @see DeleteImageMetadataUseCase 구현하는 유즈케이스 인터페이스
 * @see ImageMetadataRepositoryPort 데이터베이스 접근을 위한 아웃고잉 포트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteImageMetadataService implements DeleteImageMetadataUseCase {
    private final ImageMetadataRepositoryPort imageMetadataRepositoryPort;

    /**
     * 지정된 사용자({@code runnerId})가 소유한 이미지 메타데이터를 삭제합니다.
     *
     * <p>주어진 {@code runnerId}와 {@code imageId}를 사용하여 데이터베이스에서 메타데이터 삭제를 시도합니다.
     * 삭제된 행의 개수(deletedCount)가 0인 경우, 해당 메타데이터를 찾지 못했거나 소유권이 일치하지 않는 것으로 간주하고
     * {@link BusinessException}을 발생시켜 반응형 스트림을 종료합니다.
     * 삭제에 성공하면 반환되는 {@code Mono}는 빈 값으로 완료됩니다.</p>
     *
     * @param runnerId 삭제를 요청하는 사용자의 고유 식별자(ID)입니다. (소유권 확인에 사용됨)
     * @param imageId 삭제할 이미지 메타데이터의 고유 식별자(ID)입니다.
     * @return 삭제 작업이 성공적으로 완료되었을 때 빈 값으로 완료되는 {@code Mono<Void>} 객체입니다.
     * @throws BusinessException 주어진 {@code runnerId}와 {@code imageId}에 해당하는 메타데이터를 찾지 못하거나
     * 삭제할 수 없을 때 발생합니다.
     */
    @Override
    public Mono<Void> deleteImageMetadata(Long runnerId, Long imageId) {
        return imageMetadataRepositoryPort.deleteById(runnerId, imageId)
                .flatMap(deletedCount -> {
                    if (deletedCount == 0) {
                        return Mono.error(new BusinessException(
                                ErrorMessage.NOT_FOUND_IMAGE_METADATA,
                                "요청한 이미지 메타데이터를 찾지 못해 삭제하지 못했습니다."
                        ));
                    }
                    log.info("[DeleteImageService] 사용자 {}에 의해 이미지 {}개가 삭제되었습니다.", runnerId, deletedCount);
                    return Mono.empty();
                });
    }
}