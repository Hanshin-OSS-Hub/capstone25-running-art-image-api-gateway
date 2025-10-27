package com.aetheri.application.port.out.r2dbc;

import com.aetheri.domain.model.KakaoToken;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * {@code KakaoToken} 엔티티에 대한 비동기/논블로킹 R2DBC 데이터 접근을 제공하는 Repository 인터페이스입니다.
 *
 * <p>Spring Data R2DBC의 {@link R2dbcRepository}를 상속받아 기본 CRUD 기능을 제공하며,
 * 쿼리 메서드 정의를 통해 사용자({@code runner}) ID를 기반으로 토큰을 조회, 삭제, 확인합니다.</p>
 */
public interface KakaoTokenR2dbcRepository extends R2dbcRepository<KakaoToken, Long> {

    /**
     * 주어진 사용자 ID({@code runnerId})에 해당하는 카카오 토큰 엔티티를 조회합니다.
     *
     * @param runnerId 조회할 토큰 소유자의 고유 ID입니다.
     * @return 해당 ID와 일치하는 {@code KakaoToken} 엔티티를 발행하는 {@code Mono}입니다. 토큰이 없으면 {@code Mono.empty()}를 발행합니다.
     */
    Mono<KakaoToken> findByRunnerId(Long runnerId);

    /**
     * 주어진 사용자 ID({@code runnerId})에 해당하는 모든 카카오 토큰 정보를 데이터베이스에서 삭제합니다.
     *
     * @param runnerId 삭제할 토큰 소유자의 고유 ID입니다.
     * @return 삭제 작업 완료 시 종료되는 {@code Mono<Void>}입니다.
     */
    Mono<Void> deleteByRunnerId(Long runnerId);

    /**
     * 주어진 사용자 ID({@code runnerId})에 해당하는 카카오 토큰 정보가 존재하는지 확인합니다.
     *
     * @param runnerId 존재 여부를 확인할 토큰 소유자의 고유 ID입니다.
     * @return 토큰이 존재하면 {@code true}를, 그렇지 않으면 {@code false}를 발행하는 {@code Mono<Boolean>}입니다.
     */
    Mono<Boolean> existsByRunnerId(Long runnerId);
}