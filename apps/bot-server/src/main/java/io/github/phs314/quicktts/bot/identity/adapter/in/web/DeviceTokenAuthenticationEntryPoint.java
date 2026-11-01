package io.github.phs314.quicktts.bot.identity.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * 기기 토큰이 필요한 경로에 인증 없이 들어온 요청을 401 로 끝낸다 (ADR 0031).
 * 응답은 직접 쓰지 않고 스프링 MVC 의 예외 처리에 넘겨서, 다른 실패와 같은 오류 응답 모양(ADR 0025)이 된다.
 */
@RequiredArgsConstructor
public class DeviceTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) {
        handlerExceptionResolver.resolveException(request, response, null, new InvalidDeviceTokenException());
    }
}
