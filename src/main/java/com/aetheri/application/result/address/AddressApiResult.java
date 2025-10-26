package com.aetheri.application.result.address;

import lombok.Builder;

/**
 * 주소 검증 응답 DTO
 * */
@Builder
public record AddressApiResult(
        ResultsResult resultsResult
) {
}