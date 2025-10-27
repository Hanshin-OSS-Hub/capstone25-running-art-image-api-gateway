package com.aetheri.application.result.me;

import com.aetheri.domain.model.Runner;
import lombok.Builder;

@Builder
public record MeResult(String name) {
    public static MeResult of(Runner runner){
        return MeResult.builder()
                .name(runner.getName())
                .build();
    }
}