package com.aetheri.infrastructure.adapter.out.r2dbc.mapper;

import com.aetheri.application.result.kakao.KakaoTokenResult;
import com.aetheri.domain.model.KakaoToken;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KakaoTokenMapper {
    public static KakaoTokenResult toResult(KakaoToken entity){
        return KakaoTokenResult.builder()
                .id(entity.getId())
                .runnerId(entity.getRunnerId())
                .accessToken(entity.getAccessToken())
                .refreshToken(entity.getRefreshToken())
                .build();
    }
}
