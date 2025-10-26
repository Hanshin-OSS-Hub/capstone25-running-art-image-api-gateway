package com.aetheri.infrastructure.adapter.out.address.dto;

import lombok.Builder;

/**
 * 도로명 주소 검색 요청 DTO (record)
 */
@Builder
public record AddressSearchRequest(
        String confmKey,        // 필수: 승인키
        int currentPage,        // 필수: 현재 페이지
        int countPerPage,       // 필수: 페이지당 건수
        String keyword,         // 필수: 검색어
        String resultType,      // 결과 타입 (xml, json)
        String addInfoYn        // 추가 정보 포함 여부 (Y/N)
) {
    public static AddressSearchRequest of(String address, String apiKey) {
        return AddressSearchRequest.builder()
                .confmKey(apiKey)
                .currentPage(1)
                .countPerPage(10)
                .keyword(address)
                .resultType("json")
                .addInfoYn("N")
                .build();
    }
}