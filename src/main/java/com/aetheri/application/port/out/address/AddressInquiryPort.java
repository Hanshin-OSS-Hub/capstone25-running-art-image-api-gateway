package com.aetheri.application.port.out.address;

import com.aetheri.application.result.address.AddressApiResult;
import reactor.core.publisher.Mono;

/**
 * 외부 시스템에 주소 조회를 요청하는 아웃바운드 포트입니다.
 */
public interface AddressInquiryPort {
    
    /**
     * 키워드를 기반으로 주소를 조회합니다.
     * @param keyword 검색할 주소 키워드
     * @return 조회된 주소 정보 (없으면 Empty)
     */
    Mono<AddressApiResult> inquiryAddress(String keyword);
}