package com.aetheri.infrastructure.adapter.out.kakao.dto;

import com.aetheri.application.result.kakao.api.PartnerResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 사용자 정보 조회 API 응답에서 **{@code for_partner} 필드**를 나타내는 레코드입니다.
 *
 * <p>주로 비즈니스 파트너(카카오싱크 등)에게 제공되는 추가 정보(예: UUID)를 포함합니다.
 * {@code JsonIgnoreProperties(ignoreUnknown = true)}를 사용하여 응답 JSON에 정의되지 않은 필드는 무시합니다.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Partner(
        /**
         * 사용자를 식별하기 위한 고유 식별자(UUID)입니다.
         */
        @JsonProperty("uuid")
        String uuid
) {
        public PartnerResult toResult(){
                return PartnerResult.builder()
                        .uuid(uuid)
                        .build();
        }
}