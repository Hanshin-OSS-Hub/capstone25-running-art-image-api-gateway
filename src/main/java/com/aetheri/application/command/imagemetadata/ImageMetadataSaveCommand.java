package com.aetheri.application.command.imagemetadata;

import com.aetheri.domain.enums.image.Proficiency;
import com.aetheri.domain.enums.image.Shape;
import lombok.Builder;

/**
 * 이미지 메타데이터 저장을 요청하는 데 사용되는 레코드입니다.
 * 이 레코드는 클라이언트에서 서버로 전송되는 요청 본문(Request Body)의 구조를 정의하며,
 * 이미지에 대한 위치, 숙련도 및 형태 정보를 포함합니다.
 *
 * @param location 이미지가 촬영되거나 연관된 위치 정보입니다.
 * @param proficiency 이미지에 담긴 대상의 숙련도 또는 난이도를 나타내는 열거형(Enum) 값입니다.
 * @param shape 이미지에 담긴 대상의 형태를 나타내는 열거형(Enum) 값입니다.
 */
@Builder
public record ImageMetadataSaveCommand(
        String location,
        Proficiency proficiency,
        Shape shape
) {
}