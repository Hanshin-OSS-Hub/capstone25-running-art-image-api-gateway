package com.aetheri.infrastructure.adapter.out.address.dto;

import com.aetheri.application.result.address.CommonResult;

public record Common(
    String totalCount,
    String currentPage,
    String countPerPage,
    String errorCode,
    String errorMessage
) {
    public CommonResult toResult(){
        return CommonResult.builder()
                .totalCount(totalCount)
                .currentPage(currentPage)
                .countPerPage(countPerPage)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}