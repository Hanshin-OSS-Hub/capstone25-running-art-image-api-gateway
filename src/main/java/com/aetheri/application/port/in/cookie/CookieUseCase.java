package com.aetheri.application.port.in.cookie;

import org.springframework.http.ResponseCookie;

public interface CookieUseCase {
    ResponseCookie buildCookie(String refreshToken);
}