package com.aetheri.infrastructure.adapter.out.kakao;

import com.aetheri.application.result.kakao.KakaoIssueTokenResult;
import com.aetheri.application.port.out.kakao.KakaoRefreshTokenPort;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.infrastructure.config.properties.KakaoProperties;
import com.aetheri.infrastructure.handler.WebClientErrorHandler;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 카카오 리프레시 토큰(Refresh Token)을 사용하여 **새로운 액세스 토큰(Access Token)을 갱신**하는
 * 외부 통신 포트({@link KakaoRefreshTokenPort})의 구현체입니다.
 *
 * <p>이 서비스는 카카오 인증 서버의 {@code /oauth/token} 엔드포인트에 토큰 갱신 요청을 보냅니다.</p>
 */
@Slf4j
@Service
public class KakaoRefreshTokenAdapter implements KakaoRefreshTokenPort {
    private final String clientId;
    private final WebClient webClient;

    /**
     * {@code KakaoRefreshTokenAdapter}의 생성자입니다.
     *
     * @param webClient 카카오 인증 서버와 통신하도록 설정된 {@code WebClient} 인스턴스입니다.
     * @param kakaoProperties 카카오 애플리케이션의 설정 값들을 담고 있는 프로퍼티 객체입니다.
     */
    public KakaoRefreshTokenAdapter(
            @Qualifier("kakaoAuthWebClient") WebClient webClient,
            KakaoProperties kakaoProperties
    ) {
        this.webClient = webClient;
        this.clientId = kakaoProperties.clientId();
    }

    /**
     * 주어진 리프레시 토큰을 사용하여 카카오 액세스 토큰 갱신을 요청합니다.
     *
     * <p>요청은 다음과 같은 폼 데이터 파라미터를 포함하는 POST 요청으로 이루어집니다:</p>
     * <ul>
     * <li>{@code grant_type}: refresh_token (고정)</li>
     * <li>{@code client_id}: 애플리케이션의 REST API 키</li>
     * <li>{@code refresh_token}: 갱신에 사용될 리프레시 토큰</li>
     * </ul>
     *
     * @param refreshToken 갱신에 사용될 카카오 리프레시 토큰입니다.
     * @return 갱신된 토큰 정보를 담은 {@code KakaoTokenResponse}를 발행하는 {@code Mono} 객체입니다.
     * @throws BusinessException 리프레시 토큰이 {@code null}이거나 공백일 경우 {@code INVALID_REFRESH_TOKEN} 예외를 {@code Mono}를 통해 발생시킵니다.
     * @see <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#refresh-token">카카오 REST API - 토큰 갱신</a>
     */
    @Override
    public Mono<KakaoIssueTokenResult> refreshAccessToken(String refreshToken) {
        log.info(refreshToken);
        if (refreshToken == null || refreshToken.isBlank()) {
            return Mono.error(new BusinessException(
                    ErrorMessage.INVALID_REFRESH_TOKEN,
                    "RefreshToken이 존재하지 않음")
            );
        }

        return webClient.method(HttpMethod.POST)
                .uri("/oauth/token")
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                // 폼 데이터로 요청 본문 구성
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", clientId)
                        .with("refresh_token", refreshToken)
                )
                // WebClient 응답을 처리하고 오류를 적절히 변환합니다.
                .exchangeToMono(WebClientErrorHandler.handleErrors(KakaoIssueTokenResult.class));
    }
}