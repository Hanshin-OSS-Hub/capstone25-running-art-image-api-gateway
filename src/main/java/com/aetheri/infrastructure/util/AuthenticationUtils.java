package com.aetheri.infrastructure.util;

import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

/**
 * Spring WebFlux의 {@link ServerRequest}에서 사용자 인증 정보({@link Authentication})를
 * 추출하거나, 인증 정보에서 사용자 ID를 안전하게 추출하기 위한 **유틸리티 클래스**입니다.
 *
 * <p>주로 JWT 검증 후 {@link org.springframework.security.core.context.SecurityContext}에
 * 저장된 주체(Principal) 정보에 접근하는 데 사용됩니다.</p>
 */
@UtilityClass
public class AuthenticationUtils {
    /**
     * {@link ServerRequest}로부터 인증된 사용자의 고유 식별자(ID)를 추출합니다.
     *
     * <p>이 메서드는 {@code request.principal()}에서 {@link Authentication} 객체를 얻은 후,
     * 해당 객체의 이름(Name, 보통 JWT의 Subject에 해당하며 사용자 ID를 담고 있음)을 Long 타입으로 변환합니다.
     * 변환 중 오류가 발생하면 {@link BusinessException}을 발생시킵니다.</p>
     *
     * @param request 사용자 요청 객체({@code ServerRequest})입니다.
     * @return 인증된 사용자의 ID(Long 타입)를 발행하는 {@code Mono<Long>}입니다.
     * 인증 정보가 없으면 비어있는 {@code Mono}를 반환합니다.
     * @throws BusinessException JWT 주체(Subject)가 유효한 숫자 형태가 아닐 경우 발생합니다.
     */
    public static Mono<Long> extractRunnerIdFromRequest(ServerRequest request) {
        return request.principal()
                .ofType(Authentication.class)
                .flatMap(auth -> {
                    try {
                        // auth.getName()은 JWT Subject이며, 시스템에서는 이를 사용자 ID로 사용
                        return Mono.just(Long.parseLong(auth.getName()));
                    } catch (NumberFormatException e) {
                        return Mono.error(new BusinessException(
                                ErrorMessage.JWT_SUBJECT_IS_NOT_NUMBER,
                                "JWT subject가 숫자가 아닙니다."));
                    }
                })
                .switchIfEmpty(Mono.empty());
    }

    /**
     * {@link ServerRequest}로부터 {@link Authentication} 객체를 추출합니다.
     *
     * <p>이 메서드는 인증 주체(Principal)가 {@code Authentication} 타입임을 가정하고 캐스팅합니다.</p>
     *
     * @param request 사용자 요청 객체({@code ServerRequest})입니다.
     * @return 요청과 연결된 {@code Authentication} 객체를 발행하는 {@code Mono<Authentication>}입니다.
     */
    public static Mono<Authentication> extractAuthenticationFromRequest(ServerRequest request) {
        return request.principal()
                .cast(Authentication.class);
    }
}