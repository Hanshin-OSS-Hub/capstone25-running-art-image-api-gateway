package com.aetheri.infrastructure.adapter.out.kakao;

import com.aetheri.application.port.out.kakao.KakaoLogoutPort;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.infrastructure.handler.WebClientErrorHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 카카오 REST API의 로그아웃 엔드포인트(`v1/user/logout`)를 호출하여
 * 사용자 세션을 종료하는 외부 통신 포트({@link KakaoLogoutPort})의 구현체입니다.
 *
 * <p>이 서비스는 카카오 액세스 토큰을 무효화하여 현재 사용자의 카카오 서비스 세션을 종료합니다.</p>
 */
@Service
public class KakaoLogoutAdapter implements KakaoLogoutPort {
    private final WebClient webClient;

    /**
     * {@code KakaoLogoutAdapter}의 생성자입니다.
     *
     * @param webClient 카카오 REST API 서버와 통신하도록 설정된 {@code WebClient} 인스턴스입니다.
     */
    public KakaoLogoutAdapter(@Qualifier("kakaoWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 카카오 액세스 토큰을 사용하여 로그아웃(세션 종료 및 토큰 무효화)을 요청합니다.
     *
     * @param accessToken 카카오에서 발급해준 유효한 액세스 토큰입니다.
     * @return 로그아웃 요청 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     * @throws BusinessException 액세스 토큰이 {@code null}이거나 공백일 경우 {@code INVALID_REQUEST_PARAMETER} 예외를 {@code Mono}를 통해 발생시킵니다.
     * @see <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#logout">카카오 REST API - 로그아웃</a>
     */
    @Override
    public Mono<Void> logout(String accessToken) {
        // 액세스 토큰 유효성 검증
        if (accessToken == null || accessToken.isBlank()) {
            return Mono.error(new BusinessException(
                    ErrorMessage.INVALID_REQUEST_PARAMETER,
                    "Empty access token")
            );
        }

        return webClient.post()
                .uri("/v1/user/logout") // 카카오 로그아웃 엔드포인트
                // 액세스 토큰을 Bearer 스키마로 헤더에 설정
                .headers(h -> h.setBearerAuth(accessToken))
                // WebClient 응답을 처리하고 오류를 적절히 변환합니다. (응답 본문이 없으므로 Void 처리)
                .exchangeToMono(WebClientErrorHandler.handleErrors(Void.class));
    }
}