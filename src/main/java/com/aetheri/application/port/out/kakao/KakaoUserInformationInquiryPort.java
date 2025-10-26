package com.aetheri.application.port.out.kakao;

import com.aetheri.application.result.kakao.api.KakaoUserInfoResult;
import reactor.core.publisher.Mono;

/**
 * 카카오(Kakao) API 서버로부터 <b>사용자 회원 정보</b>를 조회하기 위한 아웃고잉 포트(Port)입니다.
 * 이 포트는 주어진 액세스 토큰을 사용하여 사용자 정보 엔드포인트에 요청을 보내는 외부 통신 구현체에 대한 추상화를 제공합니다.
 *
 * @see com.aetheri.infrastructure.adapter.out.kakao.KakaoUserInformationInquiryAdapter 실제 카카오 API 호출 구현체(어댑터)의 예시입니다.
 */
public interface KakaoUserInformationInquiryPort {

    /**
     * 카카오 회원 정보 조회 API를 호출하여 해당 사용자의 상세 정보를 요청합니다.
     *
     * <p>이 메서드는 유효한 액세스 토큰을 사용하여 카카오 계정에 등록된 사용자 ID, 프로필 정보 등
     * 필요한 정보를 DTO로 받아 반환합니다.</p>
     *
     * @param accessToken 정보를 조회할 사용자의 유효한 카카오 액세스 토큰 문자열입니다.
     * @return 조회된 카카오 사용자 정보(ID, 프로필 등)를 담은 {@code KakaoUserInfoResponseDto} 객체를 발행하는 {@code Mono}입니다.
     */
    Mono<KakaoUserInfoResult> userInformationInquiry(String accessToken);
}