package com.aetheri.infrastructure.persistence.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 데이터베이스의 {@code kakao_token} 테이블과 매핑되는 R2DBC 엔티티 클래스입니다.
 *
 * <p>이 엔티티는 특정 사용자({@code runner})가 카카오로부터 발급받은 **액세스 토큰 및 리프레시 토큰** 정보를 저장합니다.</p>
 */
@Getter
@Table("kakao_token")
@NoArgsConstructor
public class KakaoToken {
    /**
     * 카카오 토큰 정보의 고유 식별자 (Primary Key)입니다.
     */
    @Id
    @Column("id")
    private Long id;

    /**
     * 토큰 소유자(사용자)의 고유 식별자입니다. (Foreign Key)
     */
    @Column("runner_id")
    private Long runnerId;

    /**
     * 카카오 API 호출에 사용되는 액세스 토큰입니다.
     */
    @Column("access_token")
    private String accessToken;

    /**
     * 액세스 토큰을 갱신하는 데 사용되는 리프레시 토큰입니다.
     */
    @Column("refresh_token")
    private String refreshToken;

    /**
     * Builder 패턴을 사용하는 생성자입니다.
     * <p>Note: {@code id} 필드는 데이터베이스에 의해 자동 생성되므로 포함하지 않습니다.</p>
     */
    @Builder
    private KakaoToken(Long runnerId, String accessToken, String refreshToken) {
        this.runnerId = runnerId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    /**
     * 토큰 정보를 포함하는 새로운 {@code KakaoToken} 엔티티 인스턴스를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param runnerId 토큰의 소유자인 사용자의 ID입니다.
     * @param accessToken 새로 발급받은 카카오 액세스 토큰입니다.
     * @param refreshToken 새로 발급받은 카카오 리프레시 토큰입니다.
     * @return 초기화된 {@code KakaoToken} 엔티티 인스턴스입니다.
     */
    public static KakaoToken toEntity(Long runnerId, String accessToken, String refreshToken) {
        return KakaoToken.builder()
                .runnerId(runnerId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}