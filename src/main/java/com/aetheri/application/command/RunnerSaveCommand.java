package com.aetheri.application.command;

import lombok.Builder;

@Builder
public record RunnerSaveCommand(
        String name,
        Long kakaoId
) {
}