package com.aetheri.application.command.imagemetadata;

import lombok.Builder;

/**
 * 이미지 메타데이터 수정을 요청하는 데 사용되는 레코드입니다.
 * 이 레코드는 이미지의 제목과 설명을 업데이트하기 위한 정보를 담고 있으며,
 * 애플리케이션 계층의 내부 Command 객체입니다.
 * 웹 계층의 DTO는 이 Command로 변환되어 유즈케이스에 전달됩니다.
 *
 * @param title 이미지의 새로운 제목입니다.
 * @param description 이미지에 대한 새로운 간략한 설명입니다.
 */
@Builder
public record ImageMetadataUpdateCommand(
        String title,
        String description
) {
    /*
    레코드의 기본 생성자 또는 비즈니스 로직을 추가할 수 있습니다.
    참고: 자바 레코드에서는 필드에 직접 기본값을 설정할 수 없으며,
    기본값 처리는 서비스/핸들러 계층에서 수행해야 합니다.
    */
}