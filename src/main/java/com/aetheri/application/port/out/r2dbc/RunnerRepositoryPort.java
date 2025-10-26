package com.aetheri.application.port.out.r2dbc;

import com.aetheri.application.command.RunnerSaveCommand;
import com.aetheri.application.result.runner.RunnerResult;
import com.aetheri.infrastructure.adapter.out.r2dbc.RunnerRepositoryR2dbcAdapter;
import reactor.core.publisher.Mono;

/**
 * 사용자({@code Runner}) 정보의 영속성(Persistence) 처리를 위한 아웃고잉 포트(Port)입니다.
 * 이 포트는 **R2DBC**와 같은 반응형(Reactive) 데이터베이스 구현체를 통해
 * 사용자 정보를 조회, 저장, 삭제, 존재 여부를 확인하는 기능을 추상화합니다.
 *
 * @see RunnerRepositoryR2dbcAdapter 실제 R2DBC 기반 데이터 저장소 구현체(어댑터)의 예시입니다.
 */
public interface RunnerRepositoryPort {

    /**
     * 카카오 API에서 제공하는 고유 식별자({@code kakaoId})를 사용하여 사용자 정보를 조회합니다.
     *
     * @param kakaoId 카카오 서비스에서 발급된 사용자의 고유 식별자(ID)입니다.
     * @return 조회된 사용자 엔티티({@code Runner})를 발행하거나, 존재하지 않으면 비어있는 {@code Mono}를 발행합니다.
     */
    Mono<RunnerResult> findByKakaoId(Long kakaoId);

    /**
     * 주어진 사용자 엔티티 정보를 데이터베이스에 저장하거나 갱신합니다.
     *
     * <p>이 메서드는 새로운 사용자 정보를 영속화하거나, 기존 사용자 정보의 변경 사항을 반영합니다.</p>
     *
     * @param runner 저장될 사용자 엔티티({@code RunnerSaveCommand})입니다.
     * @return 저장 또는 갱신된 사용자 엔티티({@code RunnerResult})를 발행하는 {@code Mono}입니다.
     */
    Mono<RunnerResult> save(RunnerSaveCommand runner);

    /**
     * 카카오 ID({@code kakaoId})를 사용하여 해당 사용자가 데이터베이스에 존재하는지 확인합니다.
     *
     * @param kakaoId 존재 여부를 확인할 사용자의 카카오 ID입니다.
     * @return 사용자가 존재하면 {@code true}를, 존재하지 않으면 {@code false}를 발행하는 {@code Mono<Boolean>} 객체입니다.
     */
    Mono<Boolean> existsByKakaoId(Long kakaoId);

    /**
     * 시스템에서 사용하는 고유 식별자({@code id})를 사용하여 사용자 정보를 조회합니다.
     *
     * @param id 시스템 내에서 사용되는 사용자의 고유 식별자(ID)입니다.
     * @return 조회된 사용자 엔티티({@code Runner})를 발행하거나, 존재하지 않으면 비어있는 {@code Mono}를 발행합니다.
     */
    Mono<RunnerResult> findById(Long id);

    /**
     * 카카오 ID({@code kakaoId})를 사용하여 데이터베이스에 저장된 사용자 정보를 삭제합니다.
     *
     * @param kakaoId 삭제할 사용자 정보의 카카오 ID입니다.
     * @return 삭제 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    Mono<Void> deleteByKakaoId(Long kakaoId);

    /**
     * 시스템 ID({@code id})를 사용하여 데이터베이스에 저장된 사용자 정보를 삭제합니다.
     *
     * @param id 삭제할 사용자 정보의 시스템 고유 식별자(ID)입니다.
     * @return 삭제 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    Mono<Void> deleteById(Long id);
}