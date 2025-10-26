package com.aetheri.infrastructure.adapter.out.kakao;

import com.aetheri.application.port.out.kakao.KakaoUnlinkPort;
import com.aetheri.infrastructure.adapter.out.kakao.dto.UnlinkResponse;
import com.aetheri.infrastructure.util.ValidationUtils;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.infrastructure.handler.WebClientErrorHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 카카오 REST API의 연결 해제(`v1/user/unlink`) 엔드포인트를 호출하여
 * **사용자의 서비스와 카카오 계정의 연동을 해제(회원 탈퇴)**하는 외부 통신 포트({@link KakaoUnlinkPort})의 구현체입니다.
 *
 * <p>연동 해제 시, 서비스는 더 이상 해당 사용자의 카카오 정보에 접근할 수 없게 되며,
 * 기존에 발급받았던 액세스 토큰 및 리프레시 토큰은 만료 처리됩니다.</p>
 */
@Service
public class KakaoUnlinkAdapter implements KakaoUnlinkPort {
    private final WebClient webClient;

    /**
     * {@code KakaoUnlinkAdapter}의 생성자입니다.
     *
     * @param webClient 카카오 REST API 서버와 통신하도록 설정된 {@code WebClient} 인스턴스입니다.
     */
    public KakaoUnlinkAdapter(@Qualifier("kakaoWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 주어진 액세스 토큰을 사용하여 카카오 계정 연동 해제(Unlink)를 요청합니다.
     * 이는 실질적인 카카오 기반 회원 탈퇴 처리입니다.
     *
     * @param accessToken 연동 해제 대상 사용자의 액세스 토큰입니다.
     * @return 연동 해제된 사용자의 카카오 고유 ID({@code Long})를 발행하는 {@code Mono} 객체입니다.
     * @see <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#unlink">카카오 REST API - 연결 해제</a>
     */
    @Override
    public Mono<Long> unlink(String accessToken) {
        ValidationUtils.validateNotEmpty(
                accessToken,
                ErrorMessage.INVALID_REQUEST_PARAMETER,
                "액세스 토큰이 비어 있습니다."
        );

        return webClient.post()
                .uri("/v1/user/unlink") // 연동 해제 엔드포인트
                // 액세스 토큰을 Bearer 스키마로 헤더에 설정
                .headers(s -> s.setBearerAuth(accessToken))
                // WebClient 응답을 처리하고 오류를 적절히 변환합니다. (UnlinkResponse DTO로 매핑)
                .exchangeToMono(WebClientErrorHandler.handleErrors(UnlinkResponse.class))
                // 응답 DTO에서 카카오 고유 ID를 추출하여 Mono<Long>으로 변환
                .map(UnlinkResponse::id);
    }
}