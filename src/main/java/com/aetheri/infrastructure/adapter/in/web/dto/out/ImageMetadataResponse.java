package com.aetheri.infrastructure.adapter.in.web.dto.out;

import com.aetheri.application.result.imagemetadata.ImageMetadataResult;
import com.aetheri.domain.enums.image.Proficiency;
import com.aetheri.domain.enums.image.Shape;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "이미지 메타데이터 및 접근 경로를 포함하는 최종 응답 DTO")
public record ImageMetadataResponse(
        @Schema(
                description = "이미지의 현재 제목",
                example = "나의 런닝 아트"
        )
        String title,

        @Schema(
                description = "이미지에 대한 상세 설명",
                example = "2025년 10월 15일에 생성된 5km 달리기 경로 이미지입니다."
        )
        String description,

        @Schema(
                description = "이미지가 생성된 위치",
                example = "한신대학교"
        )
        String location,

        @Schema(
                description = "클라이언트가 접근 가능한 최종 이미지 URL 또는 경로",
                example = "/api/v1/image/{이미지 아이디}"
        )
        String imagePath,

        @Schema(
                description = "이미지의 주된 형태 또는 스타일",
                example = "SQUARE",             // Shape Enum의 가능한 값 중 하나
                implementation = Shape.class
        )
        Shape shape,

        @Schema(
                description = "이미지 생성 시 사용된 사용자의 숙련도 수준",
                example = "INTERMEDIATE",       // Proficiency Enum의 가능한 값 중 하나
                implementation = Proficiency.class
        )
        Proficiency proficiency,

        @Schema(
                description = "해당 이미지가 다른 사용자에게 공개(공유)되었는지 여부",
                example = "true"
        )
        Boolean shared,

        @Schema(
                description = "메타데이터가 최초로 생성된 날짜 (YYYY-MM-DD)",
                example = "2024-05-15"
        )
        LocalDate createdAt,

        @Schema(
                description = "메타데이터가 마지막으로 수정된 날짜 (YYYY-MM-DD)",
                example = "2024-05-16"
        )
        LocalDate modifiedAt
) {
        public static ImageMetadataResponse toResponse(ImageMetadataResult result) {
                return ImageMetadataResponse.builder()
                        .title(result.title())
                        .description(result.description())
                        .location(result.location())
                        .imagePath(result.imagePath())
                        .shape(result.shape())
                        .proficiency(result.proficiency())
                        .shared(result.shared())
                        .createdAt(result.createdAt())
                        .modifiedAt(result.modifiedAt())
                        .build();
        }
}