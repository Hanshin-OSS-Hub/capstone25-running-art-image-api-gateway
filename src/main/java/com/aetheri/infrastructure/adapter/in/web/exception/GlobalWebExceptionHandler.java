package com.aetheri.infrastructure.adapter.in.web.exception;

import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Spring WebFlux 환경에서 발생하는 모든 예외를 포착하여 처리하는 **전역(Global) 웹 예외 핸들러**입니다.
 *
 * <p>이 핸들러는 {@link WebExceptionHandler} 인터페이스를 구현하며,
 * 특히 {@link BusinessException}을 처리하여 클라이언트에게 일관된 JSON 형식의 오류 응답을 제공합니다.</p>
 *
 * <p>{@code @Order(-2)}를 통해 기본 예외 처리기보다 먼저 실행되도록 우선순위를 지정합니다.</p>
 */
@Slf4j
@Order(-2) // 기본 WebExceptionHandler보다 높은 우선순위를 가집니다.
@Component
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 발생한 예외({@code Throwable})를 처리하고 표준화된 오류 응답을 작성합니다.
     *
     * @param exchange 현재 요청 및 응답에 대한 정보를 담고 있는 {@code ServerWebExchange} 객체입니다.
     * @param ex 처리할 예외 인스턴스입니다.
     * @return 오류 응답 작성 완료를 나타내는 {@code Mono<Void>}입니다.
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 응답이 이미 커밋된 경우, 예외를 다시 발행하여 기본 핸들러로 전달합니다.
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status;
        String code;
        String message;

        // BusinessException인 경우, ErrorMessage의 정보를 사용합니다.
        if (ex instanceof BusinessException businessEx) {
            ErrorMessage errorMessage = businessEx.getErrorMessage();
            status = errorMessage.getStatus();
            code = errorMessage.name();
            message = businessEx.getMessage(); // BusinessException의 메시지를 사용 (Constructor 참고)
        } else {
            // 그 외 예상치 못한 모든 RuntimeException은 500 Internal Server Error로 처리합니다.
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = ErrorMessage.INTERNAL_SERVER_ERROR.name();
            message = ErrorMessage.INTERNAL_SERVER_ERROR.getMessage();
        }

        log.error("[GlobalWebExceptionHandler] 오류가 발생했습니다: [{}]: {}", code, ex.getMessage(), ex);

        // 응답 상태 코드 및 Content-Type 설정
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 클라이언트에게 보낼 JSON 응답 본문 생성
        Map<String, Object> errorBody = Map.of(
                "status", status.value(),
                "code", code,
                "message", message
        );

        try {
            // Map을 JSON 문자열로 변환하고 DataBuffer로 래핑하여 응답에 씁니다.
            String errorJson = objectMapper.writeValueAsString(errorBody);
            DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            // JSON 변환 자체에서 오류가 발생했을 경우 (매우 드문 경우), Fallback JSON을 사용합니다.
            log.error("[GlobalWebExceptionHandler] 서버에서 예외 정보를 JSON 문자열로 변환하는 과정에서 문제가 발생: {}", e.getMessage());
            String fallbackJson = "{\"status\":500,\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"예상치 못한 오류가 발생했습니다.\"}";
            DataBuffer buffer = response.bufferFactory().wrap(fallbackJson.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }
}