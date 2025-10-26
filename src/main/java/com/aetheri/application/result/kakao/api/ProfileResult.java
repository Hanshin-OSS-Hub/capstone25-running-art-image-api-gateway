package com.aetheri.application.result.kakao.api;

import lombok.Builder;

@Builder
public record ProfileResult(
        String nickName,
        String thumbnailImageUrl,
        String profileImageUrl,
        Boolean isDefaultImage,
        Boolean isDefaultNickName
) {}