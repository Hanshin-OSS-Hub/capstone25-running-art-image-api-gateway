package com.aetheri.application.port.in.address;
import reactor.core.publisher.Mono;

/**
 * 도로명 주소 API 에게 요청을 보내기 위한 포트
 * */
public interface AddressVerificationUseCase {
    /**
     *
     * @param address 검증할 주소 문자열
     * */
    Mono<Boolean> verifyAddress(String address);
}