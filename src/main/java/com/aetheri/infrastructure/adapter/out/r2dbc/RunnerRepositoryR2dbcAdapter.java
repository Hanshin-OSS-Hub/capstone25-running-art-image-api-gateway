package com.aetheri.infrastructure.adapter.out.r2dbc;

import com.aetheri.application.command.runner.RunnerSaveCommand;
import com.aetheri.application.port.out.r2dbc.RunnerRepositoryPort;
import com.aetheri.application.result.runner.RunnerResult;
import com.aetheri.infrastructure.adapter.out.r2dbc.mapper.RunnerMapper;
import com.aetheri.infrastructure.persistence.repository.RunnerR2dbcRepository;
import com.aetheri.infrastructure.persistence.entity.Runner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * {@code Runner} 엔티티에 대한 CRUD 작업을 수행하는 R2DBC 기반 데이터베이스 접근 어댑터입니다.
 *
 * <p>이 클래스는 {@link RunnerRepositoryPort} 인터페이스를 구현하며,
 * 애플리케이션의 핵심 사용자 정보(Runner)를 관리합니다.</p>
 *
 * @see RunnerRepositoryPort 사용자 데이터베이스 포트
 * @see RunnerR2dbcRepository 실제 R2DBC 리포지토리 인터페이스
 */
@Repository
@RequiredArgsConstructor
public class RunnerRepositoryR2dbcAdapter implements RunnerRepositoryPort {
    private final RunnerR2dbcRepository repository;

    /**
     * 주어진 카카오 ID({@code kakaoId})에 해당하는 사용자 엔티티를 조회합니다.
     *
     * @param kakaoId 조회할 사용자의 카카오 고유 ID입니다.
     * @return 조회된 {@code RunnerResult} 엔티티를 발행하는 {@code Mono}입니다. 사용자가 없으면 {@code Mono.empty()}를 발행합니다.
     */
    @Override
    public Mono<RunnerResult> findByKakaoId(Long kakaoId) {
        return repository.findByKakaoId(kakaoId).map(RunnerMapper::toResult);
    }

    /**
     * 새로운 사용자 엔티티를 저장하거나, 기존 사용자 엔티티를 갱신합니다.
     *
     * @param command 저장하거나 갱신할 {@code Runner} 엔티티입니다.
     * @return 저장/갱신된 {@code RunnerResult} 엔티티를 발행하는 {@code Mono}입니다.
     */
    @Override
    public Mono<RunnerResult> save(RunnerSaveCommand command) {
        Runner entity = Runner.builder()
                .name(command.name())
                .kakaoId(command.kakaoId())
                .build();
        return repository.save(entity).map(RunnerMapper::toResult);
    }

    /**
     * 주어진 카카오 ID({@code kakaoId})를 가진 사용자가 데이터베이스에 존재하는지 확인합니다.
     *
     * @param kakaoId 존재 여부를 확인할 사용자의 카카오 고유 ID입니다.
     * @return 사용자가 존재하면 {@code true}를, 그렇지 않으면 {@code false}를 발행하는 {@code Mono<Boolean>}입니다.
     */
    @Override
    public Mono<Boolean> existsByKakaoId(Long kakaoId) {
        return repository.existsByKakaoId(kakaoId);
    }

    /**
     * 주어진 애플리케이션 고유 ID({@code id})에 해당하는 사용자 엔티티를 조회합니다.
     *
     * @param id 조회할 사용자의 애플리케이션 고유 ID(PK)입니다.
     * @return 조회된 {@code RunnerResult} 엔티티를 발행하는 {@code Mono}입니다.
     */
    @Override
    public Mono<RunnerResult> findById(Long id) {
        return repository.findById(id).map(RunnerMapper::toResult);
    }

    /**
     * 주어진 카카오 ID({@code kakaoId})에 해당하는 사용자 정보를 삭제합니다.
     *
     * @param kakaoId 삭제할 사용자의 카카오 고유 ID입니다.
     * @return 삭제 작업 완료 시 종료되는 {@code Mono<Void>}입니다.
     */
    @Override
    public Mono<Void> deleteByKakaoId(Long kakaoId) {
        return repository.deleteByKakaoId(kakaoId);
    }

    /**
     * 주어진 애플리케이션 고유 ID({@code id})에 해당하는 사용자 정보를 삭제합니다.
     *
     * @param id 삭제할 사용자의 애플리케이션 고유 ID(PK)입니다.
     * @return 삭제 작업 완료 시 종료되는 {@code Mono<Void>}입니다.
     */
    @Override
    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id);
    }
}