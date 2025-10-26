package com.aetheri.application.command.runner;

import lombok.Builder;

@Builder
public record RunnerSaveCommand(
        String name,
        Long kakaoId
) {
}