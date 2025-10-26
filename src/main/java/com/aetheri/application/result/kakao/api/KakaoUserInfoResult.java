package com.aetheri.application.result.kakao.api;


import lombok.Builder;

import java.util.Date;
import java.util.HashMap;

@Builder
public record KakaoUserInfoResult(
        Long id,
        Boolean hasSignedUp,
        Date connectedAt,
        Date synchedAt,
        HashMap<String, String> properties,
        KakaoAccountResult kakaoAccountResult,
        PartnerResult partner
) {
}