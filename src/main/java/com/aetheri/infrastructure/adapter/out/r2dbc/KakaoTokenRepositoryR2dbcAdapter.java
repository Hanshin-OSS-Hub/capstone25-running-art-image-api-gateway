package com.aetheri.infrastructure.adapter.out.r2dbc;

import com.aetheri.application.port.out.r2dbc.KakaoTokenRepositoryPort;
import com.aetheri.application.port.out.r2dbc.KakaoTokenR2dbcRepository;
import com.aetheri.domain.model.KakaoToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * {@code KakaoToken} 엔티티에 대한 CRUD 작업을 수행하는 R2DBC 기반 데이터베이스 접근 어댑터입니다.
 *
 * <p>이 클래스는 {@link KakaoTokenRepositoryPort} 인터페이스를 구현하며,
 * 사용자의 카카오 액세스 토큰 및 리프레시 토큰 정보를 관리합니다.</p>
 *
 * @see KakaoTokenRepositoryPort 카카오 토큰 데이터베이스 포트
 * @see KakaoTokenR2dbcRepository 실제 R2DBC 리포지토리 인터페이스
 */
@Repository
@RequiredArgsConstructor
public class KakaoTokenRepositoryR2dbcAdapter implements KakaoTokenRepositoryPort {
    private final KakaoTokenR2dbcRepository repository;

    /**
     * 사용자 ID에 해당하는 카카오 토큰 정보를 저장하거나 갱신합니다.
     *
     * <p>토큰을 저장하기 전에 해당 {@code runnerId}로 기존 토큰이 존재하는지 확인합니다.
     * 만약 기존 토큰이 존재하면, 이를 삭제한 후 새 토큰을 저장하여 중복을 방지하고 갱신을 수행합니다.
     * 존재하지 않으면 바로 새 토큰을 저장합니다.</p>
     *
     * @param runnerId 토큰 소유자인 사용자의 고유 ID입니다.
     * @param accessToken 카카오 액세스 토큰입니다.
     * @param refreshToken 카카오 리프레시 토큰입니다.
     * @return 저장/갱신 작업 완료 시 종료되는 {@code Mono<Void>}입니다.
     */
    @Override
    public Mono<Void> save(Long runnerId, String accessToken, String refreshToken) {
        return repository.existsByRunnerId(runnerId)
                .flatMap(exists -> {
                    Mono<KakaoToken> saveMono = repository.save(
                            KakaoToken.toEntity(runnerId, accessToken, refreshToken)
                    );
                    if (exists) {
                        // 기존 토큰 삭제 후 새로 저장 (갱신 로직)
                        return repository.deleteByRunnerId(runnerId)
                                .then(saveMono)
                                .then();
                    } else {
                        // 존재하지 않으면 바로 저장
                        return saveMono.then();
                    }
                });
    }

    /**
     * 주어진 사용자 ID({@code runnerId})에 해당하는 카카오 토큰 엔티티를 조회합니다.
     *
     * @param runnerId 조회할 토큰 소유자의 고유 ID입니다.
     * @return 조회된 {@code KakaoToken} 엔티티를 발행하는 {@code Mono}입니다. 토큰이 없으면 {@code Mono.empty()}를 발행합니다.
     */
    @Override
    public Mono<KakaoToken> findByRunnerId(Long runnerId) {
        return repository.findByRunnerId(runnerId);
    }

    /**
     * 주어진 사용자 ID({@code runnerId})에 해당하는 카카오 토큰 정보를 삭제합니다.
     *
     * @param runnerId 삭제할 토큰 소유자의 고유 ID입니다.
     * @return 삭제 작업 완료 시 종료되는 {@code Mono<Void>}입니다.
     */
    @Override
    public Mono<Void> deleteByRunnerId(Long runnerId) {
        return repository.deleteByRunnerId(runnerId);
    }

    /**
     * 주어진 사용자 ID({@code runnerId})에 해당하는 카카오 토큰 정보가 존재하는지 확인합니다.
     *
     * @param runnerId 존재 여부를 확인할 토큰 소유자의 고유 ID입니다.
     * @return 토큰이 존재하면 {@code true}를, 그렇지 않으면 {@code false}를 발행하는 {@code Mono<Boolean>}입니다.
     */
    @Override
    public Mono<Boolean> existsByRunnerId(Long runnerId) {
        return repository.existsByRunnerId(runnerId);
    }
}