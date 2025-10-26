package com.aetheri.application.service.sign;

import com.aetheri.application.command.RunnerSaveCommand;
import com.aetheri.application.port.in.sign.SignUpUseCase;
import com.aetheri.application.port.out.r2dbc.RunnerRepositoryPort;
import com.aetheri.application.result.runner.RunnerResult;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 신규 사용자 회원가입 유즈케이스({@link SignUpUseCase})를 구현하는 서비스 클래스입니다.
 * 이 클래스는 카카오 인증을 통해 얻은 정보를 사용하여 시스템 데이터베이스에 새로운 사용자를 등록하는
 * 비즈니스 로직을 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignUpService implements SignUpUseCase {
    private final RunnerRepositoryPort runnerRepositoryPort;

    /**
     * 카카오 ID와 이름을 사용하여 시스템에 신규 사용자를 등록(회원가입)합니다.
     *
     * <p>회원가입 절차는 다음과 같습니다:</p>
     * <ol>
     * <li>카카오 ID로 사용자가 이미 존재하는지 확인합니다.</li>
     * <li>사용자가 **이미 존재**하면, {@code DUPLICATE_RUNNER} 예외를 발생시킵니다.</li>
     * <li>사용자가 **존재하지 않으면**, 새로운 {@code Runner} 엔티티를 생성하고 데이터베이스에 저장합니다.</li>
     * </ol>
     *
     * @param id 카카오 API를 통해 얻은 사용자의 고유 식별자(카카오 ID)입니다.
     * @param name 사용자의 초기 이름(닉네임)입니다.
     * @return 회원가입 처리가 완료되었을 때 종료되는 {@code Mono<Void>} 객체입니다.
     * @throws BusinessException 이미 동일한 카카오 ID로 가입된 사용자가 존재할 경우 {@code DUPLICATE_RUNNER} 예외를 발생시킵니다.
     * @implNote 최종적으로 {@code Mono<Void>}를 반환하는 이유는, 비즈니스 로직 후속 단계에서 저장된 엔티티 객체 자체를 필요로 하지 않기 때문입니다.
     */
    @Override
    public Mono<Void> signUp(Long id, String name) {
        // 카카오 ID로 사용자가 존재하는지 확인
        return existsByKakaoId(id)
                // 만약 이미 존재한다면, (true -> false)가 되어 스트림이 비어버림
                .filter(isExist -> !isExist)
                // 스트림이 비었을 때 (사용자가 존재할 때), 에러 발생
                .switchIfEmpty(Mono.error(new BusinessException(
                        ErrorMessage.DUPLICATE_RUNNER,
                        "이미 존재하는 사용자입니다."
                )))
                // 스트림이 비어있지 않을 때 (사용자가 존재하지 않을 때), 회원가입 진행
                .flatMap(notExists -> saveRunner(id, name))
                .then(); // Mono<Runner>를 Mono<Void>로 변환하여 반환
    }

    /**
     * 주어진 카카오 ID를 사용하여 데이터베이스에 해당 사용자가 존재하는지 확인합니다.
     *
     * @param id 조회할 사용자의 카카오 ID입니다.
     * @return 사용자가 존재하면 {@code true}를, 존재하지 않으면 {@code false}를 발행하는 {@code Mono<Boolean>}입니다.
     */
    private Mono<Boolean> existsByKakaoId(Long id) {
        return runnerRepositoryPort.existsByKakaoId(id);
    }

    /**
     * 새로운 사용자 엔티티를 생성하고 데이터베이스에 저장합니다.
     *
     * @param kakaoId 사용자의 카카오 ID입니다.
     * @param name 사용자의 이름입니다.
     * @return 저장된 {@code Runner} 엔티티를 발행하는 {@code Mono}입니다.
     */
    private Mono<RunnerResult> saveRunner(Long kakaoId, String name) {
        RunnerSaveCommand runner = RunnerSaveCommand.builder()
                .name(name)
                .kakaoId(kakaoId)
                .build();
        return runnerRepositoryPort.save(runner);
    }
}