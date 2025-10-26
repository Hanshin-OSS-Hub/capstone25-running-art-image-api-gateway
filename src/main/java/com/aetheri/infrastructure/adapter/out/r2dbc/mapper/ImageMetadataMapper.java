package com.aetheri.infrastructure.adapter.out.r2dbc.mapper;

import com.aetheri.application.result.imagemetadata.ImageMetadataResult;
import com.aetheri.infrastructure.persistence.entity.ImageMetadata;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageMetadataMapper {
    public static ImageMetadataResult toResult(ImageMetadata entity) {
        return ImageMetadataResult.builder()
                .runnerId(entity.getRunnerId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .location(entity.getLocation())
                .imagePath(entity.getImagePath())
                .shape(entity.getShape())
                .proficiency(entity.getProficiency())
                .shared(entity.getShared())
                .createdAt(entity.getCreatedAt())
                .modifiedAt(entity.getModifiedAt())
                .build();
    }
}