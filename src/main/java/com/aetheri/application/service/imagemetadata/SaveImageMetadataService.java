package com.aetheri.application.service.imagemetadata;

import com.aetheri.application.command.imagemetadata.ImageMetadataSaveCommand;
import com.aetheri.application.port.in.imagemetadata.SaveImageMetadataUseCase;
import com.aetheri.application.port.out.r2dbc.ImageMetadataRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 이미지 메타데이터 생성 유즈케이스({@link SaveImageMetadataUseCase})를 구현하는 서비스 클래스입니다.
 * 이 클래스는 특정 사용자를 대신하여 요청된 이미지 메타데이터를 데이터베이스에 영속화하는 비즈니스 로직을 수행합니다.
 *
 * @see SaveImageMetadataUseCase 구현하는 유즈케이스 인터페이스
 * @see ImageMetadataRepositoryPort 데이터베이스 접근을 위한 아웃고잉 포트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaveImageMetadataService implements SaveImageMetadataUseCase {
    private final ImageMetadataRepositoryPort imageMetadataRepositoryPort;

    /**
     * 주어진 사용자({@code runnerId})를 대신하여 이미지 메타데이터를 영속화(저장)합니다.
     *
     * <p>제공된 사용자 ID와 요청({@code request})을 사용하여 이미지 메타데이터를 구성하고,
     * 이를 이미지 저장소 포트({@code imageRepositoryPort})에 위임하여 데이터베이스에 저장합니다.
     * 저장 작업이 성공적으로 완료되면 반환되는 {@code Mono}가 빈 값으로 완료됩니다.
     * 저장소에서 발생한 모든 오류는 다운스트림으로 전파됩니다.</p>
     *
     * @param runnerId 이미지 메타데이터 생성을 요청하는 사용자의 고유 식별자(ID)입니다.
     * @param request 메타데이터 필드(위치, 형태, 숙련도 등)를 포함하는 요청 DTO입니다.
     * @return 메타데이터 영속화(저장) 작업이 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    @Override
    public Mono<Void> saveImageMetadata(Long runnerId, ImageMetadataSaveCommand request) {
        return imageMetadataRepositoryPort.saveImageMetadata(runnerId, request)
                .doOnSuccess(l -> log.info("[SaveImageMetadataService] 사용자 {}가 이미지 {}를 생성했습니다.", runnerId, l))
                .then();
    }
}