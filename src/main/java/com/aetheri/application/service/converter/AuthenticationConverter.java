package com.aetheri.application.service.converter;

import com.aetheri.infrastructure.persistence.entity.Runner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 사용자 엔티티 또는 사용자 ID를 기반으로 Spring Security의 {@link Authentication} 객체를
 * 생성하기 위한 **컨버터(Converter)** 클래스입니다.
 *
 * <p>이 클래스는 인증 정보를 기반으로 시스템 내부에서 사용되는 {@code Authentication} 객체를
 * 구성하여 보안 컨텍스트에 활용할 수 있도록 지원합니다.</p>
 */
@Service
public class AuthenticationConverter {

    /**
     * 주어진 사용자 ID({@code runnerId})를 사용하여 {@link Authentication} 객체를 생성합니다.
     *
     * <p>이 메서드는 주로 JWT(JSON Web Token) 등의 검증을 통해 사용자 ID만 알고 있을 때
     * 해당 ID를 주체(principal)로 하는 {@code Authentication} 객체를 생성하는 데 사용됩니다.
     * 권한(authorities)에는 기본값인 "ROLE_USER"가 할당됩니다.</p>
     *
     * @param runnerId 사용자 고유 식별자(ID)입니다.
     * @return 사용자의 ID와 권한 정보를 담은 {@code Authentication} 객체입니다.
     */
    public static Authentication toAuthentication(Long runnerId) {
        return new UsernamePasswordAuthenticationToken(
                runnerId,               // principal
                null,                   // credentials
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}