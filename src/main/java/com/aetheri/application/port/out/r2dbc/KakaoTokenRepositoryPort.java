package com.aetheri.application.port.out.r2dbc;

import com.aetheri.domain.model.KakaoToken;
import reactor.core.publisher.Mono;

/**
 * 카카오 토큰(액세스 토큰 및 리프레시 토큰)의 영속성(Persistence) 처리를 위한 아웃고잉 포트(Port)입니다.
 * 이 포트는 **R2DBC**와 같은 반응형(Reactive) 데이터베이스 구현체를 통해
 * 카카오 토큰 정보를 저장, 조회, 삭제, 존재 여부를 확인하는 기능을 추상화합니다.
 *
 * @see com.aetheri.infrastructure.adapter.out.r2dbc.KakaoTokenRepositoryR2dbcAdapter 실제 R2DBC 기반 데이터 저장소 구현체(어댑터)의 예시입니다.
 */
public interface KakaoTokenRepositoryPort {

    /**
     * 카카오 토큰 정보(액세스 토큰 및 리프레시 토큰)를 데이터베이스에 저장하거나 갱신합니다.
     *
     * <p>이 메서드는 {@code runnerId}와 토큰 문자열을 사용하여 해당 사용자의 토큰 정보를 영속화합니다.
     * 기존 토큰 정보가 있다면 갱신되고, 없다면 새로 저장됩니다.</p>
     *
     * @param runnerId 토큰 정보를 저장할 사용자의 고유 식별자(ID)입니다.
     * @param accessToken 카카오 액세스 토큰 문자열입니다.
     * @param refreshToken 카카오 리프레시 토큰 문자열입니다.
     * @return 저장/갱신 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    Mono<Void> save(Long runnerId, String accessToken, String refreshToken);

    /**
     * 사용자의 고유 식별자({@code runnerId})를 사용하여 저장된 카카오 토큰 정보를 조회합니다.
     *
     * @param runnerId 조회할 토큰 정보의 소유자인 사용자의 고유 식별자(ID)입니다.
     * @return 조회된 카카오 토큰 엔티티({@code KakaoToken})를 발행하거나, 존재하지 않으면 비어있는 {@code Mono}를 발행합니다.
     */
    Mono<KakaoToken> findByRunnerId(Long runnerId);

    /**
     * 사용자의 고유 식별자({@code runnerId})를 사용하여 데이터베이스에 저장된 카카오 토큰 정보를 삭제합니다.
     *
     * @param runnerId 토큰 정보를 삭제할 사용자의 고유 식별자(ID)입니다.
     * @return 삭제 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     */
    Mono<Void> deleteByRunnerId(Long runnerId);

    /**
     * 주어진 사용자 ID({@code runnerId})에 해당하는 카카오 토큰 정보가 데이터베이스에 존재하는지 확인합니다.
     *
     * @param runnerId 존재 여부를 확인할 사용자의 고유 식별자(ID)입니다.
     * @return 토큰 정보가 존재하면 {@code true}를, 존재하지 않으면 {@code false}를 발행하는 {@code Mono<Boolean>} 객체입니다.
     */
    Mono<Boolean> existsByRunnerId(Long runnerId);
}