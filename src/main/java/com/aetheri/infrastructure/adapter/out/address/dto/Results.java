package com.aetheri.infrastructure.adapter.out.address.dto;

import com.aetheri.application.result.address.ResultsResult;

import java.util.List;

public record Results(
        Common common,
        List<Juso> juso  // JSON 배열 "juso"는 List<Juso>로 매핑됩니다.
) {
    public ResultsResult toResult(){
        return ResultsResult.builder()
                .commonResult(common.toResult())
                .jusoResult(juso.stream().map(Juso::toResult).toList())
                .build();
    }

}