package com.aetheri.application.result.address;

import lombok.Builder;

@Builder
public record CommonResult(
    String totalCount,
    String currentPage,
    String countPerPage,
    String errorCode,
    String errorMessage
) {}