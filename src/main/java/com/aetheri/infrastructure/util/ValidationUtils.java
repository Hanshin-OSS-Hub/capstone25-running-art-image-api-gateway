package com.aetheri.infrastructure.util;

import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;

/**
 * 애플리케이션 내에서 입력 값의 유효성(null, 공백 여부)을 검증하고,
 * 유효하지 않은 값에 대해 일관된 {@link BusinessException}을 발생시키기 위한 **유틸리티 클래스**입니다.
 *
 * <p>이 클래스는 필수 입력 값의 누락 여부를 확인하는 데 중점을 둡니다.</p>
 */
@UtilityClass
public class ValidationUtils {
    /**
     * 주어진 문자열 값이 {@code null}이거나 비어 있는지({@code ""}) 확인하고,
     * 유효하지 않을 경우 즉시 {@link BusinessException}을 발생시킵니다. (동기적 검증)
     *
     * @param value 검증할 문자열 값입니다.
     * @param error 값이 유효하지 않을 때 사용할 {@link ErrorMessage} 타입입니다.
     * @param message 예외 발생 시 포함할 상세 오류 메시지입니다.
     * @throws BusinessException {@code value}가 {@code null}이거나 비어 있을 때 발생합니다.
     */
    public static void validateNotEmpty(String value, ErrorMessage error, String message) {
        if (value == null || value.isEmpty()) {
            throw new BusinessException(error, message);
        }
    }

    /**
     * 주어진 문자열 값이 {@code null}이거나, 비어 있거나, 공백 문자({@code " "})로만 이루어져 있는지 확인합니다. (반응형 검증)
     *
     * <p>검증에 성공하면 원본 값을 발행하는 {@code Mono<String>}을 반환하고,
     * 실패하면 {@link BusinessException}을 발생시켜 스트림을 종료합니다.</p>
     *
     * @param value 검증할 문자열 값입니다.
     * @param error 값이 유효하지 않을 때 사용할 {@link ErrorMessage} 타입입니다.
     * @param message 예외 발생 시 포함할 상세 오류 메시지입니다.
     * @return 유효한 값을 발행하는 {@code Mono<String>}입니다.
     * @throws BusinessException {@code value}가 {@code null}이거나 공백 문자열일 때 {@code Mono}를 통해 발생합니다.
     */
    public static Mono<String> validateNotBlankMono(String value, ErrorMessage error, String message) {
        return Mono.justOrEmpty(value)
                // String.isBlank()를 사용하여 null, empty, 공백 문자열을 모두 필터링합니다.
                .filter(v -> !v.isBlank())
                // 값이 필터링되어 Mono가 비게 되면, 오류 Mono를 반환합니다.
                .switchIfEmpty(Mono.error(new BusinessException(error, message)));
    }
}