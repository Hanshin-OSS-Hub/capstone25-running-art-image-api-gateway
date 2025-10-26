package com.aetheri.infrastructure.adapter.out.kakao;

import com.aetheri.application.port.out.kakao.KakaoUserInformationInquiryPort;
import com.aetheri.application.result.kakao.api.KakaoUserInfoResult;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.infrastructure.handler.WebClientErrorHandler;
import com.aetheri.infrastructure.adapter.out.kakao.dto.KakaoUserInfoResponseDto;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 카카오 REST API의 사용자 정보 조회(`v2/user/me`) 엔드포인트를 호출하여
 * **인증된 사용자의 상세 정보**를 가져오는 외부 통신 포트({@link KakaoUserInformationInquiryPort})의 구현체입니다.
 *
 * <p>이 서비스는 액세스 토큰을 기반으로 사용자 고유 ID, 닉네임, 프로필 정보 등을 요청합니다.</p>
 */
@Slf4j
@Service
public class KakaoUserInformationInquiryAdapter implements KakaoUserInformationInquiryPort {
    private final WebClient webClient;

    /**
     * {@code KakaoUserInformationInquiryAdapter}의 생성자입니다.
     *
     * @param webClient 카카오 REST API 서버와 통신하도록 설정된 {@code WebClient} 인스턴스입니다.
     */
    public KakaoUserInformationInquiryAdapter(@Qualifier("kakaoWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 카카오 액세스 토큰을 사용하여 해당 토큰의 주체인 사용자 정보를 요청하고 가져옵니다.
     *
     * @param accessToken 사용자 정보를 조회할 때 인가에 사용되는 액세스 토큰입니다.
     * @return 카카오 사용자 상세 정보({@code KakaoUserInfoResponseDto})를 발행하는 {@code Mono} 객체입니다.
     * @throws BusinessException 액세스 토큰이 {@code null}이거나 공백일 경우 {@code INVALID_REQUEST_PARAMETER} 예외를 {@code Mono}를 통해 발생시킵니다.
     * @see <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info">카카오 REST API - 사용자 정보 가져오기</a>
     */
    public Mono<KakaoUserInfoResult> userInformationInquiry(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return Mono.error(new BusinessException(
                    ErrorMessage.INVALID_REQUEST_PARAMETER,
                    "사용자 조회에 필요한 액세스 토큰이 비어있습니다."
            ));
        }

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/user/me") // 사용자 정보 조회 엔드포인트
                        .build(true))
                .headers(s -> s.setBearerAuth(accessToken)) // 액세스 토큰을 Bearer 스키마로 인가 헤더에 설정
                // 카카오 API는 CONTENT_TYPE이 필요하지 않지만, WebClient 설정을 따르기 위해 추가
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                // WebClient 응답을 처리하고 오류를 적절히 변환합니다. (KakaoUserInfoResponseDto DTO로 매핑)
                .exchangeToMono(WebClientErrorHandler.handleErrors(KakaoUserInfoResponseDto.class))
                .map(KakaoUserInfoResponseDto::toResult);
    }
}