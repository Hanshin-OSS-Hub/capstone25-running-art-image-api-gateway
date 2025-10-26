package com.aetheri.application.result.address;

import lombok.Builder;

import java.util.List;

@Builder
public record ResultsResult(
        CommonResult commonResult,
        List<JusoResult> jusoResult  // JSON 배열 "juso"는 List<Juso>로 매핑됩니다.
) {}