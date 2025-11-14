package com.aetheri.application.service.sign;

import com.aetheri.application.result.kakao.KakaoTokenAndIdResult;
import com.aetheri.application.result.kakao.KakaoIssueTokenResult;
import com.aetheri.application.result.kakao.SignInResult;
import com.aetheri.application.result.jwt.RefreshTokenIssueResult;
import com.aetheri.application.port.in.sign.SignInUseCase;
import com.aetheri.application.port.out.jwt.JwtTokenProviderPort;
import com.aetheri.application.port.out.kakao.KakaoGetAccessTokenPort;
import com.aetheri.application.port.out.kakao.KakaoUserInformationInquiryPort;
import com.aetheri.application.port.out.r2dbc.KakaoTokenRepositoryPort;
import com.aetheri.application.port.out.r2dbc.RunnerRepositoryPort;
import com.aetheri.application.port.out.token.RedisRefreshTokenRepositoryPort;
import com.aetheri.application.service.converter.AuthenticationConverter;
import com.aetheri.domain.exception.BusinessException;
import com.aetheri.domain.exception.message.ErrorMessage;
import com.aetheri.domain.model.RefreshTokenMetadata;
import com.aetheri.infrastructure.config.properties.JWTProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 카카오 인증 코드를 기반으로 로그인(Sign-In) 및 자동 회원가입을 처리하는 유즈케이스({@link SignInUseCase})의 구현체입니다.
 *
 * <p>이 서비스는 Spring WebFlux 환경에서 동작하며, 외부 카카오 API 통신, 내부 R2DBC 데이터베이스 및 Redis 저장소 접근,
 * 그리고 JWT 발급 등의 로그인 절차 전반을 논블로킹 방식으로 비동기 스트림({@code Mono})을 통해 조정합니다.</p>
 */
@Slf4j
@Service
public class SignInService implements SignInUseCase {
    private final KakaoGetAccessTokenPort kakaoGetAccessTokenPort;
    private final KakaoUserInformationInquiryPort kakaoUserInformationInquiryPort;
    private final RunnerRepositoryPort runnerRepositoryPort;
    private final KakaoTokenRepositoryPort kakaoTokenRepositoryPort;
    private final RedisRefreshTokenRepositoryPort redisRefreshTokenRepositoryPort;
    private final JwtTokenProviderPort jwtTokenProviderPort;
    private final SignUpService signUpService;

    private final long REFRESH_TOKEN_EXPIRATION_DAYS;

    /**
     * {@code SignInService}의 생성자입니다. 필요한 모든 의존성을 주입받고, JWT 설정을 초기화합니다.
     */
    public SignInService(
            KakaoGetAccessTokenPort kakaoGetAccessTokenPort,
            KakaoUserInformationInquiryPort kakaoUserInformationInquiryPort,
            RunnerRepositoryPort runnerRepositoryPort,
            KakaoTokenRepositoryPort kakaoTokenRepositoryPort, RedisRefreshTokenRepositoryPort redisRefreshTokenRepositoryPort,
            JwtTokenProviderPort jwtTokenProviderPort,
            SignUpService signUpService,
            JWTProperties jwtProperties
    ) {
        this.kakaoGetAccessTokenPort = kakaoGetAccessTokenPort;
        this.kakaoUserInformationInquiryPort = kakaoUserInformationInquiryPort;
        this.runnerRepositoryPort = runnerRepositoryPort;
        this.kakaoTokenRepositoryPort = kakaoTokenRepositoryPort;
        this.redisRefreshTokenRepositoryPort = redisRefreshTokenRepositoryPort;
        this.jwtTokenProviderPort = jwtTokenProviderPort;
        this.signUpService = signUpService;
        this.REFRESH_TOKEN_EXPIRATION_DAYS = jwtProperties.refreshTokenExpirationDays();
    }

    /**
     * 카카오 인증 코드를 사용하여 로그인 절차를 비동기적으로 수행합니다.
     *
     * <p>전체 로그인 절차는 논블로킹 스트림({@code Mono})으로 연결됩니다:</p>
     * <ol>
     * <li>인증 코드 유효성 검증 ({@code validateCode})</li>
     * <li>카카오 API로부터 액세스 토큰 획득 ({@code getKakaoToken})</li>
     * <li>카카오 액세스 토큰으로 사용자 정보 조회 및 파싱 ({@code getUserInfo})</li>
     * <li>카카오 ID로 기존 사용자 확인 또는 신규 회원가입 처리 ({@code findOrSignUpRunner})</li>
     * <li>카카오 토큰 정보를 데이터베이스에 저장/갱신 ({@code saveKakaoToken})</li>
     * <li>시스템 JWT 토큰(액세스/리프레시) 발급 및 Redis에 리프레시 토큰 저장 ({@code issueTokensAndSave})</li>
     * <li>최종 응답 DTO 반환 ({@code toSignInResponse})</li>
     * </ol>
     *
     * @param code 카카오가 로그인 성공 후 리다이렉션 시 발급한 인증 코드 문자열입니다.
     * @return 로그인에 성공하면 시스템 JWT 토큰 정보를 담은 {@code SignInResult}를 발행하는 {@code Mono}입니다.
     */
    @Override
    public Mono<SignInResult> signIn(String code) {
        // 1. 코드가 유효한지 검증하고 스트림을 시작합니다.
        return validateCode(code)
                // 2. 카카오에서 액세스 토큰을 비동기로 가져옵니다.
                .flatMap(this::getKakaoToken)
                // 3. 액세스 토큰으로 사용자 정보를 비동기로 가져와 파싱합니다.
                .flatMap(this::getUserInfo)
                // 4. 카카오 ID로 사용자를 찾거나, 없으면 회원가입을 수행합니다.
                .flatMap(this::findOrSignUpRunner)
                // 5. 카카오 토큰 정보를 데이터베이스에 저장/갱신합니다.
                .flatMap(this::saveKakaoToken)
                // 6. 서버의 JWT 토큰을 발급하고 리프레시 토큰을 Redis에 저장합니다.
                .flatMap(this::issueTokensAndSave)
                // 7. 최종 응답 DTO로 변환하여 스트림을 종료합니다.
                .map(this::toSignInResponse);
    }

    /**
     * 인증 코드({@code code})의 유효성(null 또는 공백이 아닌지)을 검증합니다.
     *
     * @param code 검증할 인증 코드입니다.
     * @return 코드가 유효하면 코드를 발행하는 {@code Mono<String>}입니다.
     * @throws BusinessException 코드가 유효하지 않다면 {@code NOT_FOUND_AUTHORIZATION_CODE} 예외를 발행합니다.
     */
    private Mono<String> validateCode(String code) {
        return Mono.fromSupplier(() -> code)
                .filter(c -> c != null && !c.trim().isEmpty())
                .switchIfEmpty(Mono.error(new BusinessException(
                        ErrorMessage.NOT_FOUND_AUTHORIZATION_CODE,
                        "카카오 인증 코드를 찾을 수 없습니다."
                )));
    }

    /**
     * 카카오 인증 서버에 액세스 토큰 발급을 비동기로 요청합니다.
     *
     * @param code 카카오에서 발급한 로그인 인증 코드입니다.
     * @return 카카오의 토큰 응답({@code KakaoIssueTokenResult})을 발행하는 {@code Mono}입니다.
     * @throws BusinessException 카카오 API 호출 실패 또는 토큰이 없는 경우 {@code NOT_FOUND_ACCESS_TOKEN} 예외를 발행합니다.
     */
    private Mono<KakaoIssueTokenResult> getKakaoToken(String code) {
        return kakaoGetAccessTokenPort.tokenRequest(code)
                .switchIfEmpty(Mono.error(new BusinessException(
                        ErrorMessage.NOT_FOUND_ACCESS_TOKEN,
                        "카카오로부터 액세스 토큰을 획득하지 못했습니다."
                )));

    }

    /**
     * 카카오 액세스 토큰을 사용하여 사용자 정보를 비동기로 조회하고, 내부 DTO로 변환합니다.
     *
     * <p>사용자 정보 DTO에서 닉네임을 추출할 때, {@code properties} -> {@code kakaoAccount.profile} 순서로 시도하며,
     * 모두 실패할 경우 기본값(`runner-카카오ID`)을 사용합니다.</p>
     *
     * @param dto 카카오의 액세스/리프레시 토큰이 담긴 DTO입니다.
     * @return 카카오 토큰 정보와 사용자 ID, 파싱된 닉네임을 담은 {@code KakaoTokenAndIdResult}를 발행하는 {@code Mono}입니다.
     * @throws BusinessException 사용자 정보를 조회하지 못했다면 {@code NOT_FOUND_RUNNER} 예외를 발행합니다.
     */
    private Mono<KakaoTokenAndIdResult> getUserInfo(KakaoIssueTokenResult dto) {
        return kakaoUserInformationInquiryPort.userInformationInquiry(dto.accessToken())
                .switchIfEmpty(Mono.error(new BusinessException(
                        ErrorMessage.NOT_FOUND_RUNNER,
                        "카카오에서 사용자 정보를 찾을 수 없습니다."
                )))
                .map(userInfo -> {
                    // 사용자 닉네임 추출 로직
                    String name = java.util.Optional.ofNullable(userInfo.properties())
                            .map(p -> Optional.ofNullable(p.get("nickname")))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .filter(s -> !s.isBlank())
                            .orElseGet(() -> {
                                var acc = userInfo.kakaoAccountResult();
                                var profile = (acc != null) ? acc.profile() : null;
                                var nick = (profile != null) ? profile.nickName() : null;
                                return (nick != null && !nick.isBlank()) ? nick : ("runner-" + userInfo.id());
                            });
                    return new KakaoTokenAndIdResult(dto.accessToken(), dto.refreshToken(), userInfo.id(), name);
                });
    }

    /**
     * 카카오 사용자 ID를 기반으로 데이터베이스에서 사용자를 찾거나, 존재하지 않으면 자동 회원가입을 수행합니다.
     *
     * <p>R2DBC 리포지토리를 사용하여 사용자 존재 여부를 비동기로 확인하고,
     * 존재하지 않을 경우 {@code signUpService}를 통해 회원가입을 먼저 완료한 후 사용자 정보를 조회합니다.</p>
     *
     * @param dto 카카오 토큰 정보와 카카오 사용자 ID, 이름이 담긴 DTO입니다.
     * @return 시스템 DB의 ID로 갱신된 {@code KakaoTokenAndIdResult}를 발행하는 {@code Mono}입니다.
     */
    private Mono<KakaoTokenAndIdResult> findOrSignUpRunner(KakaoTokenAndIdResult dto) {
        return runnerRepositoryPort.existsByKakaoId(dto.id())
                .flatMap(exists -> exists ?
                        // 1. 사용자 존재 시: 사용자 정보 조회 (로그인)
                        runnerRepositoryPort.findByKakaoId(dto.id()) :
                        // 2. 사용자 미존재 시: 회원가입 수행 후, 완료를 기다려 사용자 정보 조회
                        signUpService.signUp(dto.id(), dto.name())
                                .then(runnerRepositoryPort.findByKakaoId(dto.id()))
                ).map(runner -> new KakaoTokenAndIdResult(
                        dto.accessToken(),
                        dto.refreshToken(),
                        runner.getId(), // 카카오 ID 대신 시스템 DB의 Runner ID로 갱신
                        runner.getName())
                );
    }

    /**
     * 카카오 토큰 정보(액세스/리프레시)를 데이터베이스에 비동기로 저장하거나 갱신합니다.
     *
     * @param dto 카카오 토큰과 시스템 사용자 ID가 담긴 DTO입니다.
     * @return 저장/갱신에 성공하면 해당 사용자의 시스템 ID를 발행하는 {@code Mono<Long>}입니다.
     */
    private Mono<Long> saveKakaoToken(KakaoTokenAndIdResult dto) {
        return kakaoTokenRepositoryPort.save(
                        dto.id(), // 이 시점의 ID는 시스템 Runner ID임
                        dto.accessToken(),
                        dto.refreshToken()
                )
                .thenReturn(dto.id());
    }

    /**
     * 새로운 시스템 JWT 토큰(액세스/리프레시)을 생성하고, 리프레시 토큰 메타데이터를 Redis에 저장합니다.
     *
     * <p>JWT 생성은 블로킹 작업이므로, 이 메서드 호출 시점에서는 **블로킹 격리**가 필요합니다.
     * 생성된 리프레시 토큰 자체를 Redis의 키로 사용하고, 만료 시간을 포함한 메타데이터를 값으로 저장합니다.</p>
     *
     * @param runnerId JWT 토큰 생성 및 저장에 사용될 사용자의 시스템 ID입니다.
     * @return 생성된 토큰 쌍을 담은 {@code TokenBundle}을 발행하는 {@code Mono}입니다.
     */
    private Mono<TokenBundle> issueTokensAndSave(Long runnerId) {
        // 1. 사용자 ID로 Authentication 객체 생성 (JWT 생성에 필요)
        Authentication auth = AuthenticationConverter.toAuthentication(runnerId);

        // 2. 액세스 토큰과 리프레시 토큰을 생성합니다. (JWT 생성은 블로킹 작업이므로, 호출 스택에서 격리되어야 함)
        String accessToken = jwtTokenProviderPort.generateAccessToken(auth);
        RefreshTokenIssueResult refreshTokenIssueResult = jwtTokenProviderPort.generateRefreshToken(auth);

        // 3. Redis 저장을 위한 메타데이터 객체 생성
        RefreshTokenMetadata refreshTokenMetadata = RefreshTokenMetadata.of(refreshTokenIssueResult.userId(), refreshTokenIssueResult.expire());

        return redisRefreshTokenRepositoryPort
                // 4. 생성된 리프레시 토큰 문자열(토큰 값 자체)을 키로, 메타데이터를 값으로 Redis에 비동기 저장합니다.
                .saveRefreshToken(refreshTokenIssueResult.refreshToken(), refreshTokenMetadata)
                // 5. Redis 저장이 완료되면, 액세스/리프레시 토큰 번들 DTO를 발행하여 다음 단계로 전달합니다.
                .thenReturn(new TokenBundle(accessToken, refreshTokenIssueResult.refreshToken()));
    }

    /**
     * 이 서비스 내에서만 사용할 JWT 토큰 번들 운반 DTO (Java Record)
     */
    private record TokenBundle(
            String accessToken,
            String refreshToken
    ) {
    }

    /**
     * JWT 토큰 번들 DTO를 최종 응답 {@code SignInResult} DTO로 변환합니다.
     *
     * @param tokenBundle JWT 토큰 쌍이 담긴 내부 DTO입니다.
     * @return 최종 로그인 응답 DTO입니다.
     */
    private SignInResult toSignInResponse(TokenBundle tokenBundle) {
        // 리프레시 토큰의 만료 시간을 초 단위로 계산합니다.
        long expiresInSeconds = 60 * 60 * 24 * REFRESH_TOKEN_EXPIRATION_DAYS;
        return new SignInResult(tokenBundle.accessToken(), tokenBundle.refreshToken(), expiresInSeconds);
    }
}