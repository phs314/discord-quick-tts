package io.github.phs314.quicktts.bot.config;

import io.github.phs314.quicktts.api.QuickTtsApi;
import io.github.phs314.quicktts.bot.identity.adapter.in.web.DeviceTokenAuthenticationEntryPoint;
import io.github.phs314.quicktts.bot.identity.adapter.in.web.DeviceTokenAuthenticationFilter;
import io.github.phs314.quicktts.bot.identity.application.port.in.AuthenticateDeviceUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * 웹 API 의 인증 규칙 (ADR 0031). 기기 등록, 상태 확인, API 문서만 토큰 없이 열고, 나머지는 모두 기기 토큰이 있어야 한다.
 * 클라이언트는 매 요청에 토큰을 싣는 프로그램이라 세션, 로그인 화면, CSRF 토큰을 쓰지 않는다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            AuthenticateDeviceUseCase authenticateDeviceUseCase,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.POST, QuickTtsApi.DEVICES_PATH).permitAll()
                        .requestMatchers(HttpMethod.GET, QuickTtsApi.HEALTH_PATH).permitAll()
                        // API 문서는 경로와 요청 모양만 보여 주고, 문서에서 API 를 부를 때는 기기 토큰이 있어야 한다 (ADR 0054).
                        .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new DeviceTokenAuthenticationEntryPoint(handlerExceptionResolver)))
                .addFilterBefore(new DeviceTokenAuthenticationFilter(authenticateDeviceUseCase),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
