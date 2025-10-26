package com.aetheri.application.service.imgfetch;

import com.aetheri.application.port.in.imgfetch.ImagePathValidateUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 이미지 경로 유효성 검증 유즈케이스({@link ImagePathValidateUseCase})를 구현하는 서비스 클래스입니다.
 * 이 클래스는 주어진 경로 문자열이 보안적으로 안전하고 파일 시스템 규칙 및 서비스 정책(예: 허용된 확장자)에
 * 부합하는 유효한 이미지 경로인지 확인하는 비즈니스 로직을 수행합니다.
 */
@Slf4j
@Service
public class ImagePathValidateService implements ImagePathValidateUseCase {
    /**
     * 주어진 이미지 경로 문자열의 유효성을 검증합니다.
     *
     * <p>검증 순서는 다음과 같습니다:</p>
     * <ol>
     * <li>null 또는 공백 여부 확인 ({@link #isNull(String)})</li>
     * <li>경로 조작을 유발하는 특수 문자 포함 여부 확인 ({@link #isContainIllegalChar(String)})</li>
     * <li>파일 이름 및 허용 확장자 화이트리스트 기반 검증 ({@link #isValidFilename(String)})</li>
     * </ol>
     *
     * @param path 유효성을 검증할 이미지 파일의 경로(또는 파일 이름) 문자열입니다.
     * @return 경로가 유효하다면 {@code true}를, 그렇지 않다면 {@code false}를 발행하는 {@code Mono<Boolean>} 객체입니다.
     */
    @Override
    public Mono<Boolean> isValidatePath(String path) {
        if(isNull(path)) return Mono.just(false);
        if(isContainIllegalChar(path)) return Mono.just(false);

        return Mono.just(isValidFilename(path));
    }

    /**
     * 파일 이름이 {@code null}이거나 비어있는 문자열({@code null} 또는 공백만 포함)인지 확인합니다.
     *
     * @param filename 검사할 파일 이름입니다.
     * @return 파일 이름이 null 또는 공백이면 {@code true}를 반환합니다.
     */
    private boolean isNull(String filename) {
        return filename == null || filename.isBlank();
    }

    /**
     * 경로 조작 공격(Path Traversal Attack)을 방지하기 위해 파일 이름에 부적절한 문자열을 포함하는지 확인합니다.
     *
     * <p>확인 대상 문자열: {@code ..}, {@code /}, {@code \}</p>
     *
     * @param filename 검사할 파일 이름입니다.
     * @return 파일 이름에 불법적인 경로 문자가 포함되어 있으면 {@code true}를 반환합니다.
     */
    private boolean isContainIllegalChar(String filename) {
        return filename.contains("..") || filename.contains("/") || filename.contains("\\");
    }

    /**
     * 파일 이름의 문자열 구성 및 파일 확장자를 화이트리스트 기반으로 검증합니다.
     *
     * <p>허용되는 문자 패턴: 영문 대소문자, 숫자, 점(.), 밑줄(\_), 하이픈(-)으로 구성되어야 합니다.
     * 허용되는 확장자: {@code .png}, {@code .jpg}, {@code .jpeg}입니다.</p>
     *
     * @param filename 검사할 파일 이름입니다.
     * @return 파일 이름의 구성 및 확장자가 유효하면 {@code true}를 반환합니다.
     */
    private boolean isValidFilename(String filename) {
        // 기본 문자 검증: 영문 대소문자, 숫자, ., _, - 만 허용
        if (!filename.matches("^[A-Za-z0-9._-]+$")) {
            return false;
        }
        // 허용된 이미지 확장자 검증
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".png") ||
                lowerCaseFilename.endsWith(".jpg") ||
                lowerCaseFilename.endsWith(".jpeg");
    }
}