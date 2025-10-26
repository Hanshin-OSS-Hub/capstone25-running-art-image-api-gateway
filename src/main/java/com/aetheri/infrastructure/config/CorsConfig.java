package com.aetheri.infrastructure.config;

import com.aetheri.infrastructure.config.properties.JWTProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {
    
    @Bean
    public CorsWebFilter corsWebFilter(JWTProperties jwtProperties) {
        CorsConfiguration config = new CorsConfiguration();
        
        // 개발 환경에서 자주 사용하는 origin 패턴
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:*",           // 로컬 개발 서버
            "http://127.0.0.1:*",           // 로컬호스트
            "http://192.168.*.*:*",         // 로컬 네트워크
            "http://10.*.*.*:*"             // 내부 네트워크
        ));
        
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of(jwtProperties.accessTokenHeader(), "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}