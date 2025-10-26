package com.aetheri.infrastructure.adapter.out.kakao.dto;

import com.aetheri.application.result.kakao.api.ProfileResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 사용자 정보 조회 API 응답에서 {@code kakao_account} 내부의 **{@code profile} 필드**를 나타내는 레코드입니다.
 *
 * <p>사용자의 닉네임, 프로필 이미지 URL 등 기본 프로필 정보를 포함합니다.
 * {@code JsonIgnoreProperties(ignoreUnknown = true)}를 사용하여 응답 JSON에 정의되지 않은 필드는 무시합니다.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Profile(

        /**
         * 사용자의 닉네임입니다.
         */
        @JsonProperty("nickname")
        String nickName,

        /**
         * 110x110 크기의 프로필 미리보기 이미지 URL입니다.
         */
        @JsonProperty("thumbnail_image_url")
        String thumbnailImageUrl,

        /**
         * 480x480 크기의 프로필 사진 URL입니다.
         */
        @JsonProperty("profile_image_url")
        String profileImageUrl,

        /**
         * 프로필 사진이 카카오에서 제공하는 **기본 이미지**인지 여부입니다.
         * ({@code true}: 기본 이미지, {@code false}: 사용자가 등록한 이미지)
         */
        @JsonProperty("is_default_image")
        Boolean isDefaultImage,

        /**
         * 닉네임이 카카오에서 제공하는 **기본 닉네임**인지 여부입니다.
         * ({@code true}: 기본 닉네임, {@code false}: 사용자가 등록한 닉네임)
         */
        @JsonProperty("is_default_nickname")
        Boolean isDefaultNickName
) {
        public ProfileResult toResult(){
                return ProfileResult.builder()
                        .nickName(nickName)
                        .thumbnailImageUrl(thumbnailImageUrl)
                        .profileImageUrl(profileImageUrl)
                        .isDefaultImage(isDefaultImage)
                        .isDefaultNickName(isDefaultNickName)
                        .build();
        }
}