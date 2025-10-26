package com.aetheri.application.result.imagemetadata;

import com.aetheri.domain.enums.image.Proficiency;
import com.aetheri.domain.enums.image.Shape;
import lombok.Builder;

import java.time.LocalDate;

/**
 * 이미지 메타데이터 응답을 위한 레코드입니다.
 * 이 레코드는 클라이언트에게 이미지에 대한 상세 정보 및 접근 경로를 제공합니다.
 *
 * @param title 이미지의 제목입니다.
 * @param description 이미지에 대한 상세 설명입니다.
 * @param location 이미지가 생성된 위치(장소)에 대한 정보입니다.
 * @param imagePath 클라이언트가 이미지를 직접 조회할 수 있는 REST API 경로 또는 URL입니다.
 * @param shape 이미지의 주된 형태(예: SQUARE, RECTANGLE)를 나타내는 열거형 값입니다.
 * @param proficiency 이미지를 생성한 사용자의 숙련도 수준을 나타내는 열거형 값입니다.
 * @param shared 해당 이미지가 외부 사용자에게 공유(공개) 상태인지 여부를 나타냅니다.
 * @param createdAt 메타데이터가 최초로 생성된 날짜입니다.
 * @param modifiedAt 메타데이터가 마지막으로 수정된 날짜입니다.
 */
@Builder
public record ImageMetadataResult(
        Long runnerId,
        String title,
        String description,
        String location,
        String imagePath,
        Shape shape,
        Proficiency proficiency,
        Boolean shared,
        LocalDate createdAt,
        LocalDate modifiedAt
) {
}