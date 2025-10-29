package com.aetheri.application.port.out.r2dbc;

import com.aetheri.domain.model.Runner;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * {@code Runner} 엔티티(사용자 정보)에 대한 비동기/논블로킹 R2DBC 데이터 접근을 제공하는 Repository 인터페이스입니다.
 *
 * <p>Spring Data R2DBC의 {@link R2dbcRepository}를 상속받아 기본 CRUD 기능을 제공하며,
 * 쿼리 메서드 정의를 통해 카카오 고유 ID({@code kakaoId})를 기반으로 사용자를 조회, 확인, 삭제합니다.</p>
 */
public interface RunnerR2dbcRepository extends R2dbcRepository<Runner, Long> {

    /**
     * 주어진 카카오 고유 ID({@code kakaoId})와 일치하는 사용자({@code Runner}) 엔티티를 조회합니다.
     *
     * @param kakaoId 조회할 사용자의 카카오 고유 ID입니다.
     * @return 해당 ID와 일치하는 {@code Runner} 엔티티를 발행하는 {@code Mono}입니다. 사용자가 없으면 {@code Mono.empty()}를 발행합니다.
     */
    Mono<Runner> findByKakaoId(Long kakaoId);

    /**
     * 주어진 카카오 고유 ID({@code kakaoId})를 가진 사용자가 데이터베이스에 존재하는지 확인합니다.
     *
     * @param kakaoId 존재 여부를 확인할 사용자의 카카오 고유 ID입니다.
     * @return 사용자가 존재하면 {@code true}를, 그렇지 않으면 {@code false}를 발행하는 {@code Mono<Boolean>}입니다.
     */
    Mono<Boolean> existsByKakaoId(Long kakaoId);

    /**
     * 주어진 카카오 고유 ID({@code kakaoId})에 해당하는 사용자 정보를 데이터베이스에서 삭제합니다.
     *
     * <p>주로 회원 탈퇴 시 사용됩니다.</p>
     *
     * @param kakaoId 삭제할 사용자의 카카오 고유 ID입니다.
     * @return 삭제 작업 완료 시 종료되는 {@code Mono<Void>}입니다.
     */
    Mono<Void> deleteByKakaoId(Long kakaoId);
}