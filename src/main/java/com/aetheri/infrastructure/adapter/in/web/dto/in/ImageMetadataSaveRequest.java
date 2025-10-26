package com.aetheri.infrastructure.adapter.in.web.dto.in;

import com.aetheri.application.command.imagemetadata.ImageMetadataSaveCommand;
import com.aetheri.domain.enums.image.Proficiency;
import com.aetheri.domain.enums.image.Shape;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;

/**
 * 이미지 메타데이터 저장을 요청하는 데 사용되는 레코드입니다.
 * 이 레코드는 클라이언트에서 서버로 전송되는 요청 본문(Request Body)의 구조를 정의하며,
 * 이미지에 대한 위치, 숙련도 및 형태 정보를 포함합니다.
 *
 * @param location    이미지가 촬영되거나 연관된 위치 정보입니다.
 * @param proficiency 이미지에 담긴 대상의 숙련도 또는 난이도를 나타내는 열거형(Enum) 값입니다.
 * @param shape       이미지에 담긴 대상의 형태를 나타내는 열거형(Enum) 값입니다.
 * @see ImageMetadataSaveCommand
 */
public record ImageMetadataSaveRequest(
        @NotNull
        @Schema(
                description = "런닝 아트가 생성된 위치",
                example = "한신대학교",
                requiredMode = RequiredMode.REQUIRED
        )
        String location,

        @NotNull
        @Schema(
                description = "이미지 생성에 사용된 사용자의 숙련도 수준",
                example = "BEGINNER",                   // 해당 Enum의 가능한 값 중 하나를 예시로 둡니다.
                implementation = Proficiency.class,     // 가능한 값의 목록을 명시적으로 표시합니다.
                requiredMode = RequiredMode.REQUIRED
        )
        Proficiency proficiency,

        @NotNull
        @Schema(
                description = "이미지의 주된 형태 또는 스타일",
                example = "SQUARE",                 // 해당 Enum의 가능한 값 중 하나를 예시로 둡니다.
                implementation = Shape.class,       // 가능한 값의 목록을 명시적으로 표시합니다.
                requiredMode = RequiredMode.REQUIRED
        )
        Shape shape
) {
    public ImageMetadataSaveCommand toCommand() {
        return ImageMetadataSaveCommand.builder()
                .location(location)
                .proficiency(proficiency)
                .shape(shape)
                .build();
    }
}