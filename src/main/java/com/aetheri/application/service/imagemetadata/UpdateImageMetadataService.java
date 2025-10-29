package com.aetheri.application.service.imagemetadata;

import com.aetheri.application.command.imagemetadata.ImageMetadataUpdateCommand;
import com.aetheri.application.port.in.imagemetadata.UpdateImageMetadataUseCase;
import com.aetheri.application.port.out.r2dbc.ImageMetadataRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 이미지 메타데이터 수정 유즈케이스({@link UpdateImageMetadataUseCase})를 구현하는 서비스 클래스입니다.
 * 이 클래스는 특정 사용자의 요청에 따라 기존 이미지 메타데이터를 업데이트하는 비즈니스 로직을 수행합니다.
 *
 * @see UpdateImageMetadataUseCase 구현하는 유즈케이스 인터페이스
 * @see ImageMetadataRepositoryPort 데이터베이스 접근을 위한 아웃고잉 포트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateImageMetadataService implements UpdateImageMetadataUseCase {
    private final ImageMetadataRepositoryPort imageMetadataRepositoryPort;

    /**
     * 주어진 사용자를 대신하여 기존 이미지의 메타데이터를 수정합니다.
     *
     * <p>이미지의 소유자(요청 사용자)만이 이 업데이트를 수행할 수 있으며,
     * 해당 권한 확인/강제 적용 로직은 하위 저장소 포트(repository/port)로 위임됩니다.</p>
     *
     * @param runnerId 업데이트를 요청하는 사용자의 고유 식별자(ID)입니다. (소유권 확인에 사용됨)
     * @param imageId 메타데이터를 수정할 이미지의 고유 식별자(ID)입니다.
     * @param request 업데이트할 메타데이터 필드(예: 제목, 설명)가 포함된 요청 DTO입니다.
     * @return 업데이트 작업이 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     * (저장소 호출에서 발생하는 오류는 반환된 {@code Mono}를 통해 전파됩니다.)
     */
    @Override
    public Mono<Void> updateImageMetadata(Long runnerId, Long imageId, ImageMetadataUpdateCommand request) {
        return imageMetadataRepositoryPort.updateImageMetadata(runnerId, imageId, request)
                .doOnSuccess(l -> log.info("[UpdateImageMetadataService] 사용자 {}가 이미지 {}의 메타데이터를 수정했습니다. 바뀐 행: {}", runnerId, imageId, l))
                .then();
    }
}