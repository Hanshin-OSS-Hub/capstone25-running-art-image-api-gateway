package com.aetheri.infrastructure.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 데이터베이스의 {@code runner} 테이블과 매핑되는 R2DBC 엔티티 클래스입니다.
 *
 * <p>이 엔티티는 애플리케이션에 가입한 **사용자(Runner)**의 기본 정보, 특히 카카오 소셜 로그인 정보를 저장합니다.</p>
 */
@Getter
@Table("runner")
@NoArgsConstructor
public class Runner {
    /**
     * 사용자의 고유 식별자 (Primary Key)입니다.
     */
    @Id
    @Column("id")
    private Long id;

    /**
     * 카카오로부터 발급받은 사용자의 고유 식별 ID입니다.
     */
    @Column("kakao_id")
    private Long kakaoId;

    /**
     * 사용자의 이름 또는 닉네임입니다.
     */
    @Column("name")
    private String name;

    /**
     * 새로운 {@code Runner} 엔티티를 생성하기 위한 생성자입니다.
     *
     * <p>주로 카카오 로그인 후 신규 사용자를 등록할 때 사용됩니다.</p>
     *
     * @param kakaoId 카카오 고유 ID입니다.
     * @param name 사용자의 이름 또는 닉네임입니다.
     */
    public Runner(Long kakaoId, String name) {
        this.kakaoId = kakaoId;
        this.name = name;
    }
}