package com.aetheri.infrastructure.adapter.out.kakao;

import com.aetheri.application.result.kakao.KakaoIssueTokenResult;
import com.aetheri.application.port.out.kakao.KakaoGetAccessTokenPort;
import com.aetheri.infrastructure.config.properties.KakaoProperties;
import com.aetheri.infrastructure.handler.WebClientErrorHandler;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 카카오 로그인 인증 코드({@code code})를 사용하여 **액세스 토큰(Access Token) 및 리프레시 토큰(Refresh Token)을
 * 발급받기 위한 외부 통신 포트({@link KakaoGetAccessTokenPort})의 구현체**입니다.
 *
 * <p>이 서비스는 카카오 인증 서버의 `/oauth/token` 엔드포인트에 요청을 보냅니다.</p>
 */
@Service
public class KakaoGetAccessTokenAdapter implements KakaoGetAccessTokenPort {
    private final String clientId;
    private final WebClient webClient;

    /**
     * {@code KakaoGetAccessTokenAdapter}의 생성자입니다.
     *
     * @param webClient 카카오 인증 서버와 통신하도록 설정된 {@code WebClient} 인스턴스입니다.
     * @param kakaoProperties 카카오 애플리케이션의 설정 값들을 담고 있는 프로퍼티 객체입니다.
     */
    public KakaoGetAccessTokenAdapter(
            @Qualifier("kakaoAuthWebClient") WebClient webClient,
            KakaoProperties kakaoProperties
    ) {
        this.webClient = webClient;
        this.clientId = kakaoProperties.clientId();
    }

    /**
     * 카카오 인증 코드({@code code})를 사용하여 액세스 토큰 발급을 요청합니다.
     *
     * <p>요청은 다음과 같은 파라미터를 포함하는 POST 요청으로 이루어집니다:</p>
     * <ul>
     * <li>{@code grant_type}: authorization_code (고정)</li>
     * <li>{@code client_id}: 애플리케이션의 REST API 키</li>
     * <li>{@code code}: 카카오 로그인 후 받은 인증 코드</li>
     * </ul>
     *
     * @param code 카카오에서 발급해준 인증 코드입니다.
     * @return 카카오 토큰 응답({@code KakaoTokenResponse})을 발행하는 {@code Mono} 객체입니다.
     * @see <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token">카카오 REST API - 토큰 요청</a>
     */
    @Override
    public Mono<KakaoIssueTokenResult> tokenRequest(String code) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token") // 토큰 요청 엔드포인트
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("code", code)
                        // build()를 사용하여 쿼리 파라미터를 안전하게 인코딩합니다.
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                // WebClient 응답을 처리하고 오류를 적절히 변환합니다.
                .exchangeToMono(WebClientErrorHandler.handleErrors(KakaoIssueTokenResult.class));
    }
}