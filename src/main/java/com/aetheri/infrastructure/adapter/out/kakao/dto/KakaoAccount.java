package com.aetheri.infrastructure.adapter.out.kakao.dto;

import com.aetheri.application.result.kakao.api.KakaoAccountResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * 카카오 사용자 정보 조회 API 응답에서 **{@code kakao_account} 필드**를 나타내는 레코드입니다.
 *
 * <p>사용자의 카카오 계정 정보, 프로필, 그리고 각 정보 항목에 대한 동의 여부를 포함합니다.
 * {@code JsonIgnoreProperties(ignoreUnknown = true)}를 사용하여 응답 JSON에 정의되지 않은 필드는 무시합니다.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoAccount(

        // ------------------------------------ 동의 여부 필드 ------------------------------------

        /**
         * 프로필 정보 제공 동의 여부입니다.
         */
        @JsonProperty("profile_needs_agreement")
        Boolean isProfileAgree,

        /**
         * 닉네임 제공 동의 여부입니다.
         */
        @JsonProperty("profile_nickname_needs_agreement")
        Boolean isNickNameAgree,

        /**
         * 프로필 사진 제공 동의 여부입니다.
         */
        @JsonProperty("profile_image_needs_agreement")
        Boolean isProfileImageAgree,

        // ------------------------------------ 계정 정보 필드 ------------------------------------

        /**
         * 사용자의 상세 프로필 정보입니다.
         */
        @JsonProperty("profile")
        Profile profile,

        /**
         * 이름 제공 동의 여부입니다.
         */
        @JsonProperty("name_needs_agreement")
        Boolean isNameAgree,

        /**
         * 카카오계정에 등록된 사용자 이름입니다.
         */
        @JsonProperty("name")
        String name,

        // ------------------------------------ 이메일 필드 ------------------------------------

        /**
         * 이메일 주소 제공 동의 여부입니다.
         */
        @JsonProperty("email_needs_agreement")
        Boolean isEmailAgree,

        /**
         * 카카오계정 이메일이 유효한지 여부입니다.
         */
        @JsonProperty("is_email_valid")
        Boolean isEmailValid,

        /**
         * 카카오계정 이메일이 인증되었는지 여부입니다.
         */
        @JsonProperty("is_email_verified")
        Boolean isEmailVerified,

        /**
         * 카카오계정의 대표 이메일 주소입니다.
         */
        @JsonProperty("email")
        String email,

        // ------------------------------------ 기타 개인 정보 필드 ------------------------------------

        /**
         * 연령대 제공 동의 여부입니다.
         */
        @JsonProperty("age_range_needs_agreement")
        Boolean isAgeAgree,

        /**
         * 연령대 정보입니다. (예: "20~29")
         */
        @JsonProperty("age_range")
        String ageRange,

        /**
         * 출생 연도 제공 동의 여부입니다.
         */
        @JsonProperty("birthyear_needs_agreement")
        Boolean isBirthYearAgree,

        /**
         * 출생 연도입니다. (YYYY 형식의 문자열)
         */
        @JsonProperty("birthyear")
        String birthYear,

        /**
         * 생일 제공 동의 여부입니다.
         */
        @JsonProperty("birthday_needs_agreement")
        Boolean isBirthDayAgree,

        /**
         * 생일입니다. (MMDD 형식의 문자열)
         */
        @JsonProperty("birthday")
        String birthDay,

        /**
         * 생일 타입입니다. (SOLAR 또는 LUNAR)
         */
        @JsonProperty("birthday_type")
        String birthDayType,

        /**
         * 성별 제공 동의 여부입니다.
         */
        @JsonProperty("gender_needs_agreement")
        Boolean isGenderAgree,

        /**
         * 성별 정보입니다. (예: "male", "female")
         */
        @JsonProperty("gender")
        String gender,

        /**
         * 전화번호 제공 동의 여부입니다.
         */
        @JsonProperty("phone_number_needs_agreement")
        Boolean isPhoneNumberAgree,

        /**
         * 전화번호입니다. (+82 00-0000-0000 형식의 문자열)
         */
        @JsonProperty("phone_number")
        String phoneNumber,

        // ------------------------------------ CI 필드 ------------------------------------

        /**
         * CI(Connecting Information, 연계 정보) 제공 동의 여부입니다.
         */
        @JsonProperty("ci_needs_agreement")
        Boolean isCIAgree,

        /**
         * CI 값입니다. (서비스 간 사용자 연계를 위한 고유 값)
         */
        @JsonProperty("ci")
        String ci,

        /**
         * CI가 발급된 시각입니다. (UTC 기준)
         */
        @JsonProperty("ci_authenticated_at")
        Date ciCreatedAt
) {
        public KakaoAccountResult toResult(){
                return KakaoAccountResult.builder()
                        .isProfileAgree(isProfileAgree)
                        .isNickNameAgree(isNickNameAgree)
                        .isProfileImageAgree(isProfileImageAgree)
                        .profile(profile != null ? profile.toResult() : null)
                        .isNameAgree(isNameAgree)
                        .name(name)
                        .isEmailAgree(isEmailAgree)
                        .isEmailValid(isEmailValid)
                        .isEmailVerified(isEmailVerified)
                        .email(email)
                        .isAgeAgree(isAgeAgree)
                        .ageRange(ageRange)
                        .isBirthYearAgree(isBirthYearAgree)
                        .birthYear(birthYear)
                        .isBirthDayAgree(isBirthDayAgree)
                        .birthDay(birthDay)
                        .birthDayType(birthDayType)
                        .isGenderAgree(isGenderAgree)
                        .gender(gender)
                        .isPhoneNumberAgree(isPhoneNumberAgree)
                        .phoneNumber(phoneNumber)
                        .isCIAgree(isCIAgree)
                        .ci(ci)
                        .ciCreatedAt(ciCreatedAt)
                        .build();
        }
}