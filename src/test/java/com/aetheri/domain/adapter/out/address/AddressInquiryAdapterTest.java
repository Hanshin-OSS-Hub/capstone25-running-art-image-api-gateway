package com.aetheri.domain.adapter.out.address;

import com.aetheri.infrastructure.adapter.out.address.AddressInquiryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test") // <-- 이 부분을 추가
class AddressInquiryAdapterTest {

    @Autowired
    private AddressInquiryAdapter addressInquiryAdapter;

    @Test
    public void success_inquiry_address() {
        // given
        String address = "한신대학교";

        // when
        var mono = addressInquiryAdapter.inquiryAddress(address);
        mono.subscribe(System.out::println);

        // then
        StepVerifier.create(mono)
                .expectNextMatches(response -> response != null
                        && response.results() != null
                        && response.results().juso() != null
                        && response.results().juso().size() == 2
                )
                .verifyComplete();  // 스트림이 성공적으로 완료되는지 검증 (이때까지 테스트가 대기함)
    }

    @Test
    public void success_when_address_not_found() {
        // given
        String address = "존재하지않는주소123456";

        // when
        var mono = addressInquiryAdapter.inquiryAddress(address);

        // then
        StepVerifier.create(mono)
                .expectNextMatches(response -> response.results().juso().isEmpty())
                .verifyComplete();
    }
}