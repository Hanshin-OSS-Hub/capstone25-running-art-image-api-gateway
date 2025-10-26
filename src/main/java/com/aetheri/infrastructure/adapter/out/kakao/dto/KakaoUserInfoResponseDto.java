package com.aetheri.infrastructure.adapter.out.kakao.dto;

import com.aetheri.application.result.kakao.api.KakaoUserInfoResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;

/**
 * 카카오 사용자 정보 조회 API(예: {@code /v2/user/me})의 **전체 응답 전문**을 나타내는 레코드입니다.
 *
 * <p>사용자 고유 ID, 서비스 연결 시점, 그리고 상세 계정 정보({@code kakao_account}) 등을 포함합니다.
 * {@code JsonIgnoreProperties(ignoreUnknown = true)}를 사용하여 응답 JSON에 정의되지 않은 필드는 무시합니다.</p>
 */
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoUserInfoResponseDto(

        /**
         * 카카오 서비스 내에서 사용자를 고유하게 식별하는 **회원 번호(사용자 ID)**입니다.
         */
        @JsonProperty("id")
        Long id,

        /**
         * 앱 연결 상태를 나타냅니다.
         * <p>자동 연결 설정을 비활성화한 경우에만 이 필드가 존재합니다.
         * {@code true}는 연결 상태, {@code false}는 연결 대기 상태를 의미합니다.</p>
         */
        @JsonProperty("has_signed_up")
        Boolean hasSignedUp,

        /**
         * 사용자가 현재 서비스에 연결을 완료한 시각입니다. (UTC 기준)
         */
        @JsonProperty("connected_at")
        Date connectedAt,

        /**
         * 카카오싱크 간편가입을 통해 로그인한 시각입니다. (UTC 기준)
         */
        @JsonProperty("synched_at")
        Date synchedAt,

        /**
         * 사용자 동의 항목 외에 개발자가 추가로 설정한 사용자 프로퍼티(속성)입니다.
         */
        @JsonProperty("properties")
        HashMap<String, String> properties,

        /**
         * 사용자의 상세 카카오 계정 정보 및 각 항목의 동의 여부를 담고 있는 객체입니다.
         *
         * @see KakaoAccount
         */
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount,

        /**
         * 비즈니스 파트너를 위한 추가 정보입니다. (예: UUID 등)
         */
        @JsonProperty("for_partner")
        Partner partner
) {
    public KakaoUserInfoResult toResult() {
        return KakaoUserInfoResult.builder()
                .id(id)
                .hasSignedUp(hasSignedUp)
                .connectedAt(connectedAt)
                .synchedAt(synchedAt)
                .properties(properties == null ? null : new HashMap<>(properties))
                .kakaoAccountResult(kakaoAccount != null ? kakaoAccount.toResult() : null)
                .partner(partner != null ? partner.toResult() : null)
                .build();

    }
}