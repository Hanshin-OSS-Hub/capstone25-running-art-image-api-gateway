package com.aetheri.infrastructure.persistence.entity;

import com.aetheri.domain.enums.image.Proficiency;
import com.aetheri.domain.enums.image.Shape;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

/**
 * 데이터베이스의 {@code image_metadata} 테이블과 매핑되는 R2DBC 엔티티 클래스입니다.
 *
 * <p>이 엔티티는 사용자가 생성한 **러닝 아트 이미지의 메타데이터 정보**를 저장합니다.</p>
 */
@Table("image_metadata")
@Getter
@NoArgsConstructor
public class ImageMetadata {
    /**
     * 이미지 메타데이터의 고유 식별자 (Primary Key)입니다.
     */
    @Id
    @Column("id")
    private Long id;

    /**
     * 이미지를 생성한 사용자의 고유 식별자입니다. (Foreign Key)
     */
    @Column("runner_id")
    private Long runnerId;

    /**
     * 이미지 파일이 저장된 스토리지 상의 경로 또는 고유 식별 키입니다.
     */
    @Column("image_path")
    private String imagePath;

    /**
     * 사용자가 지정한 이미지의 제목입니다.
     */
    @Column("title")
    private String title;

    /**
     * 사용자가 지정한 이미지에 대한 설명입니다.
     */
    @Column("description")
    private String description;

    /**
     * 이미지가 생성된 위치(지역) 정보입니다.
     */
    @Column("location")
    private String location;

    /**
     * 러닝 아트 이미지의 형태({@link Shape})입니다.
     */
    @Column("shape")
    private Shape shape;

    /**
     * 러닝 아트 이미지 생성 시 달성한 난이도 또는 숙련도({@link Proficiency}) 레벨입니다.
     */
    @Column("proficiency")
    private Proficiency proficiency;

    /**
     * 이미지가 공개적으로 공유되었는지 여부입니다. (공유 상태: {@code true}, 비공개: {@code false})
     */
    @Column("shared")
    private Boolean shared;

    /**
     * 메타데이터가 데이터베이스에 처음 생성된 날짜입니다.
     */
    @Column("created_at")
    private LocalDate createdAt;

    /**
     * 메타데이터가 마지막으로 수정된 날짜입니다.
     */
    @Column("modified_at")
    private LocalDate modifiedAt;

    /**
     * Builder 패턴을 사용하는 전체 필드 생성자입니다.
     */
    @Builder
    public ImageMetadata(Long runnerId, String imagePath, String title, String description, String location, Shape shape, Proficiency proficiency, Boolean shared, LocalDate createdAt, LocalDate modifiedAt) {
        this.runnerId = runnerId;
        this.imagePath = imagePath;
        this.title = title;
        this.description = description;
        this.location = location;
        this.shape = shape;
        this.proficiency = proficiency;
        this.shared = shared;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    /**
     * 필수 정보만을 사용하여 새로운 {@code ImageMetadata} 엔티티 인스턴스를 생성하는 정적 팩토리 메서드입니다.
     *
     * <p>초기 제목과 설명, 생성/수정 날짜 및 공유 상태를 기본값으로 설정합니다.</p>
     *
     * @param runnerId 이미지를 생성한 사용자의 ID입니다.
     * @param imagePath 이미지 파일의 고유 경로입니다.
     * @param location 이미지 생성 위치 정보입니다.
     * @param shape 이미지의 형태 정보입니다.
     * @param proficiency 이미지의 숙련도 정보입니다.
     * @return 초기화된 {@code ImageMetadata} 엔티티 인스턴스입니다.
     */
    public static ImageMetadata toEntity(Long runnerId, String imagePath, String location, Shape shape, Proficiency proficiency) {
        return ImageMetadata.builder()
                .runnerId(runnerId)
                .imagePath(imagePath)
                .title(location + " " + shape.name()) // 초기 제목 설정
                .description("설명을 추가해주세요")     // 기본 설명 설정
                .location(location)
                .shape(shape)
                .proficiency(proficiency)
                .createdAt(LocalDate.now())
                .shared(false)                      // 기본적으로 비공개(false)로 설정
                .modifiedAt(LocalDate.now())
                .build();
    }
}