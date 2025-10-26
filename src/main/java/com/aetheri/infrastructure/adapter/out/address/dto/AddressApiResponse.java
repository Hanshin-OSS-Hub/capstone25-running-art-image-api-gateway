package com.aetheri.infrastructure.adapter.out.address.dto;

import com.aetheri.application.result.address.AddressApiResult;

/**
 * 주소 검증 응답 DTO
 * */
public record AddressApiResponse(
        Results results
) {
    public AddressApiResult toResult(){
        return AddressApiResult.builder()
                .resultsResult(results.toResult())
                .build();
    }
}