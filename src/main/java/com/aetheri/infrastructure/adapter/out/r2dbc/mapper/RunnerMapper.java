package com.aetheri.infrastructure.adapter.out.r2dbc.mapper;

import com.aetheri.application.result.runner.RunnerResult;
import com.aetheri.domain.model.Runner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RunnerMapper {
    public static RunnerResult toResult(Runner entity){
        return RunnerResult.builder()
                .id(entity.getId())
                .kakaoId(entity.getKakaoId())
                .name(entity.getName())
                .build();
    }
}
