package com.aetheri.application.result.kakao.api;

import lombok.Builder;

import java.util.Date;

@Builder
public record KakaoAccountResult(
        Boolean isProfileAgree,
        Boolean isNickNameAgree,
        Boolean isProfileImageAgree,
        ProfileResult profile,
        Boolean isNameAgree,
        String name,
        Boolean isEmailAgree,
        Boolean isEmailValid,
        Boolean isEmailVerified,
        String email,
        Boolean isAgeAgree,
        String ageRange,
        Boolean isBirthYearAgree,
        String birthYear,
        Boolean isBirthDayAgree,
        String birthDay,
        String birthDayType,
        Boolean isGenderAgree,
        String gender,
        Boolean isPhoneNumberAgree,
        String phoneNumber,
        Boolean isCIAgree,
        String ci,
        Date ciCreatedAt
) {}