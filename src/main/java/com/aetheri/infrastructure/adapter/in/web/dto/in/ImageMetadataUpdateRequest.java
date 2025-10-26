package com.aetheri.infrastructure.adapter.in.web.dto.in;

import com.aetheri.application.command.imagemetadata.ImageMetadataUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;

/**
 * 이미지 메타데이터 수정을 요청하는 데 사용되는 레코드입니다.
 * 이 레코드는 이미지의 제목과 설명을 업데이트하기 위한 정보를 담고 있으며,
 * 요청 본문(Request Body)으로 사용됩니다.
 *
 * @param title       이미지의 새로운 제목입니다.
 * @param description 이미지에 대한 새로운 간략한 설명입니다.
 * @see ImageMetadataUpdateCommand
 */
public record ImageMetadataUpdateRequest(
        @NotNull
        @Schema(
                description = "이미지의 새로운 제목. 이 값은 필수로 제공되어야 합니다.",
                example = "겨울의 런닝 아트",
                requiredMode = RequiredMode.REQUIRED
        )
        String title,

        @Schema(
                description = "이미지에 대한 상세 설명. 비어있을 수 있으며, 제공하지 않을 경우 '설명 없음'으로 처리됩니다.",
                example = "2024년 5월 10일에 달린 5km 코스",
                requiredMode = RequiredMode.NOT_REQUIRED // 필수가 아님을 명시합니다.
        )
        String description
) {
    public ImageMetadataUpdateCommand toCommand() {
        return ImageMetadataUpdateCommand.builder()
                .title(title)
                .description(description)
                .build();
    }
}