package com.aetheri.application.result.runner;

import com.aetheri.application.result.me.MeResult;
import lombok.Builder;

@Builder
public record RunnerResult(
        Long id,
        Long kakaoId,
        String name
) {
    public MeResult toMeResult() {
        return MeResult.builder()
                .name(name)
                .build();
    }
}