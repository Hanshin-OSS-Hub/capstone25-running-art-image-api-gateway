package com.aetheri.infrastructure.adapter.in.web.handler;

import com.aetheri.application.port.in.cookie.CookieUseCase;
import com.aetheri.application.port.in.sign.SignInUseCase;
import com.aetheri.application.port.in.sign.SignOffUseCase;
import com.aetheri.application.port.in.sign.SignOutUseCase;
import com.aetheri.infrastructure.util.AuthenticationUtils;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.infrastructure.config.properties.JWTProperties;
import com.aetheri.infrastructure.config.properties.KakaoProperties;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 사용자 인증 및 권한 부여와 관련된 **HTTP 요청을 처리하는 핸들러 클래스**입니다.
 *
 * <p>카카오 소셜 로그인을 통한 인증, JWT 토큰 발급, 로그아웃, 회원 탈퇴 등의 비즈니스 로직을
 * {@code UseCase} 포트를 통해 실행하고, 그 결과를 {@code ServerResponse}로 변환하여 응답합니다.</p>
 */
@Slf4j
@Component
public class KakaoAuthHandler {
    private final SignInUseCase signInUseCase;
    private final SignOffUseCase signOffUseCase;
    private final SignOutUseCase signOutUseCase;
    private final CookieUseCase cookieUseCase;

    private final String clientId;
    private final String redirectUri;
    private final String refreshTokenCookie;
    private final String accessTokenHeader;

    /**
     * {@code AuthHandler}의 의존성 주입 생성자입니다.
     *
     * <p>인증 관련 유스케이스와 설정({@code KakaoProperties}, {@code JWTProperties})으로부터
     * 필요한 속성들을 초기화합니다.</p>
     */
    public KakaoAuthHandler(
            SignInUseCase signInUseCase,
            SignOffUseCase signOffUseCase,
            SignOutUseCase signOutUseCase,
            CookieUseCase cookieUseCase,
            KakaoProperties kakaoProperties,
            JWTProperties jwtProperties
    ) {
        this.signInUseCase = signInUseCase;
        this.signOffUseCase = signOffUseCase;
        this.signOutUseCase = signOutUseCase;
        this.cookieUseCase = cookieUseCase;

        this.clientId = kakaoProperties.clientId();
        this.redirectUri = kakaoProperties.redirectUri();
        this.refreshTokenCookie = jwtProperties.refreshTokenCookie();
        this.accessTokenHeader = jwtProperties.accessTokenHeader();
    }

    /**
     * 클라이언트를 카카오 로그인 페이지로 리다이렉트하여 인가 코드(Authorization Code) 요청을 수행합니다.
     *
     * <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-code">카카오 REST API 문서 참조</a>
     *
     * @param request 현재 서버 요청 정보입니다.
     * @return 카카오 인가 URL로 리다이렉트하는 {@code ServerResponse}입니다.
     */
    public Mono<ServerResponse> redirectToKakaoLogin(ServerRequest request) {
        String kakaoAuthUrl = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .toUriString();

        // 307 Temporary Redirect 응답을 반환합니다.
        return ServerResponse.temporaryRedirect(URI.create(kakaoAuthUrl)).build();
    }

    /**
     * 카카오로부터 전달받은 인가 코드(Code)를 사용하여 액세스 토큰을 발급받고 로그인(회원가입 포함)을 처리하는 핸들러입니다.
     *
     * <p>로그인 성공 후, 액세스 토큰은 응답 헤더에, 리프레시 토큰은 HTTP Only 쿠키에 설정하여 응답합니다.</p>
     *
     * @param request URL 쿼리 파라미터로 인가 코드를 포함하는 서버 요청 정보입니다.
     * @return 인증 정보를 포함하는 {@code ServerResponse}입니다.
     */
    public Mono<ServerResponse> getKakaoAccessToken(ServerRequest request) {

        return findCodeFromUrl(request) // 1. 요청에서 인가 코드 추출
                .flatMap(signInUseCase::signIn) // 2. 로그인 처리 (토큰 발급 및 사용자 등록)
                .flatMap(response -> { // 3. 응답 처리 및 토큰 설정
                    log.info("[AuthHandler] 로그인 성공: \naccessToken={} \n refreshToken={}", response.accessToken(), response.refreshToken());

                    // 액세스 토큰은 응답 헤더에 설정
                    return ServerResponse.ok()
                            .header(accessTokenHeader, response.accessToken())
                            .cookie(cookieUseCase.buildCookie(response.refreshToken()))
                            .body(Mono.empty(), Void.class);
                });
    }

    /**
     * 서비스 회원탈퇴 요청을 처리합니다.
     *
     * <p>인증된 사용자 ID를 추출하여 {@code SignOffUseCase}를 실행하고 204 No Content 응답을 반환합니다.</p>
     *
     * @param request 현재 서버 요청 정보입니다.
     * @return 처리 완료 시 204 No Content를 반환하는 {@code ServerResponse}입니다.
     */
    public Mono<ServerResponse> signOff(ServerRequest request) {
        return AuthenticationUtils.extractRunnerIdFromRequest(request)
                .flatMap(runnerId -> {
                    log.info("[AuthHandler] signOff: runnerId={}", runnerId);
                    return signOffUseCase.signOff(runnerId).then(ServerResponse.noContent().build());
                });
    }

    /**
     * 회원 로그아웃 요청을 처리합니다.
     *
     * <p>인증된 사용자 ID를 추출하여 {@code SignOutUseCase}를 실행하여 JWT 토큰을 무효화하고 Kakao 로그아웃을 수행한 후 204 No Content 응답을 반환합니다.</p>
     *
     * @param request 현재 서버 요청 정보입니다.
     * @return 처리 완료 시 204 No Content를 반환하는 {@code ServerResponse}입니다.
     */
    public Mono<ServerResponse> signOut(ServerRequest request) {
        return AuthenticationUtils.extractRunnerIdFromRequest(request)
                .flatMap(runnerId -> {
                    log.info("[AuthHandler] signOut: runnerId={}", runnerId);
                    return signOutUseCase.signOut(runnerId).then(ServerResponse.noContent().build());
                });
    }


    /**
     * 요청 URL의 쿼리 파라미터에서 **인가 코드({@code code})**를 추출합니다.
     *
     * @param request 인가 코드를 포함할 수 있는 서버 요청입니다.
     * @return 인가 코드를 발행하는 {@code Mono<String>}입니다. 코드가 없으면 {@code BusinessException}을 발생시킵니다.
     */
    private Mono<String> findCodeFromUrl(ServerRequest request) {
        return Mono.just(request.queryParam("code")
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorMessage.NOT_FOUND_AUTHORIZATION_CODE,
                                "인증 코드를 응답에서 찾을 수 없습니다."
                        )
                ));
    }
}