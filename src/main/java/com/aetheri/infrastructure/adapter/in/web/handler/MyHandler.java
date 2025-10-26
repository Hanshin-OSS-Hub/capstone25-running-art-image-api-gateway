package com.aetheri.infrastructure.adapter.in.web.handler;

import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 간단한 테스트 및 예시용 **WebFlux 함수형 엔드포인트 핸들러** 클래스입니다.
 *
 * <p>기본적인 문자열 응답과 의도적인 오류 발생 메서드를 포함하여
 * 함수형 라우팅과 전역 예외 처리 테스트를 위해 사용됩니다.</p>
 */
@Component
public class MyHandler {

    /**
     * 클라이언트에게 "Hello, World!" 메시지를 JSON 형식으로 응답하는 핸들러입니다.
     *
     * @param request 현재 서버 요청 정보입니다.
     * @return 메시지를 포함하는 200 OK {@code ServerResponse}를 발행하는 {@code Mono}입니다.
     */
    public Mono<ServerResponse> hello(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("message", "Hello, World!"));
    }

    /**
     * **의도적으로 {@link BusinessException}을 발생시켜** 전역 예외 처리기가 작동하는지 테스트하기 위한 핸들러입니다.
     *
     * <p>항상 {@code INTERNAL_SERVER_ERROR}를 반환하도록 설정되어 있습니다.</p>
     *
     * @param request 현재 서버 요청 정보입니다.
     * @return 예외를 발생시키는 {@code Mono<ServerResponse>}입니다.
     */
    public Mono<ServerResponse> helloError(ServerRequest request) {
        // 의도적으로 예외 발생
        return Mono.error(
                new BusinessException(
                        ErrorMessage.INTERNAL_SERVER_ERROR,
                        "서버에서 발생한 오류"
                )
        );
    }
}