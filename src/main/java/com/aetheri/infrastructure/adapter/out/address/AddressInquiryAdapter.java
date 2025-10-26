package com.aetheri.infrastructure.adapter.out.address;

import com.aetheri.application.result.address.AddressApiResult;
import com.aetheri.infrastructure.adapter.out.address.dto.AddressSearchRequest;
import com.aetheri.infrastructure.adapter.out.address.dto.AddressApiResponse;
import com.aetheri.application.port.out.address.AddressInquiryPort;
import com.aetheri.infrastructure.config.properties.AddressVerificationProperties;
import com.aetheri.infrastructure.handler.WebClientErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 도로명 주소 API (juso.go.kr)를 호출하여
 * AddressInquiryPort를 구현하는 아웃바운드 어댑터입니다.
 */
@Slf4j
@Component
public class AddressInquiryAdapter implements AddressInquiryPort {

    private final WebClient webClient;
    private final String apiKey;

    public AddressInquiryAdapter(
            @Qualifier("addressWebClient") WebClient webClient,
            AddressVerificationProperties properties
    ) {
        this.webClient = webClient;
        this.apiKey = properties.key();
    }

    @Override
    public Mono<AddressApiResult> inquiryAddress(String keyword) {

        var request = AddressSearchRequest.of(keyword, apiKey);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("confmKey", request.confmKey())
                        .queryParam("currentPage", request.currentPage())
                        .queryParam("countPerPage", request.countPerPage())
                        .queryParam("keyword", request.keyword())
                        .queryParam("resultType", request.resultType())
                        .build()
                )
                .exchangeToMono(WebClientErrorHandler.handleErrors(AddressApiResponse.class))
                .mapNotNull(AddressApiResponse::toResult);
    }
}