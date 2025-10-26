package com.aetheri.application.service.imgfetch;

import com.aetheri.application.port.in.imgfetch.ImageFetchUseCase;
import com.aetheri.application.port.in.imgfetch.ImagePathValidateUseCase;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.infrastructure.config.properties.ImageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;

/**
 * 서버에 저장된 이미지를 조회하는 유즈케이스({@link ImageFetchUseCase})를 구현하는 서비스 클래스입니다.
 * 이 클래스는 요청된 경로의 유효성을 검증한 후, {@link ResourceLoader}를 사용하여 이미지 파일을 {@link Resource} 형태로 로드합니다.
 */
@Slf4j
@Service
public class ImageFetchService implements ImageFetchUseCase {
    private final ResourceLoader resourceLoader;
    private final ImagePathValidateUseCase imagePathValidator;
    private final String locationPattern;

    /**
     * {@code ImageFetchPort}의 생성자입니다.
     *
     * @param resourceLoader     이미지 리소스를 로드하는 데 사용되는 Spring의 리소스 로더입니다.
     * @param imagePathValidator 이미지 경로의 유효성을 검증하는 유즈케이스입니다.
     * @param imageProperties    이미지 파일 저장 경로 설정값을 담고 있는 프로퍼티 객체입니다.
     */
    public ImageFetchService(
            ResourceLoader resourceLoader,
            ImagePathValidateUseCase imagePathValidator,
            ImageProperties imageProperties
    ) {
        this.resourceLoader = resourceLoader;
        this.imagePathValidator = imagePathValidator;
        // 이미지 프로퍼티에서 실제 파일 저장 경로 패턴을 가져옵니다.
        this.locationPattern = imageProperties.path();
    }

    /**
     * 주어진 경로({@code path})에서 이미지 파일을 조회합니다.
     *
     * <p>요청된 경로에서 파일 이름을 추출한 후, 경로 유효성 검증을 먼저 수행합니다.
     * 유효성 검증에 실패하거나 이미지를 찾지 못하면 {@link BusinessException}을 발생시키고,
     * 성공하면 {@link Resource} 객체를 반환합니다.</p>
     *
     * @param path 이미지를 조회할 파일 시스템 또는 저장소 내의 경로입니다.
     * @return 조회된 이미지 파일 데이터를 담고 있는 {@code Resource} 객체를 발행하는 {@code Mono}입니다.
     * @throws BusinessException 경로가 유효하지 않을 때(INVALID\_IMAGE\_PATH) 또는 이미지를 찾지 못했을 때(NOT\_FOUND\_IMAGE) 발생합니다.
     */
    @Override
    public Mono<Resource> fetchImage(String path) {
        // 경로에서 순수 파일 이름만 추출합니다.
        String fileName = Paths.get(path).getFileName().toString();

        return imagePathValidator.isValidatePath(fileName)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(
                                new BusinessException(
                                        ErrorMessage.INVALID_IMAGE_PATH,
                                        "유효한 이미지 요청이 아닙니다."
                                )
                        );
                    }
                    return fetchImageFromStore(fileName);
                })
                .switchIfEmpty(
                        Mono.error(
                                new BusinessException(
                                        ErrorMessage.NOT_FOUND_IMAGE,
                                        "이미지를 조회하지 못했습니다."
                                ))
                )
                .doOnSuccess(l -> log.info("이미지 조회에 성공했습니다."));
    }

    private Mono<Resource> fetchImageFromStore(String fileName) {
        Resource resource = resourceLoader.getResource(locationPattern + fileName);
        if (!resource.exists()) {
            return Mono.error(
                    new BusinessException(
                            ErrorMessage.NOT_FOUND_IMAGE,
                            "이미지를 조회하지 못했습니다."
                    )
            );
        }
        return Mono.just(resource);
    }
}