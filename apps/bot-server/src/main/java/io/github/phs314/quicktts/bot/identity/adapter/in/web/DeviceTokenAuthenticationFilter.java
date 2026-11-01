package io.github.phs314.quicktts.bot.identity.adapter.in.web;

import io.github.phs314.quicktts.api.QuickTtsApi;
import io.github.phs314.quicktts.bot.identity.application.port.in.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.identity.application.port.in.query.AuthenticateDeviceQuery;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@code Authorization: Bearer <기기 토큰>} 으로 그 PC 를 연결한 디스코드 사용자를 알아내 스프링 시큐리티에 알린다 (ADR 0031).
 * 사용자는 {@link DiscordUserId} 로 실어서, 컨트롤러는 {@code @AuthenticationPrincipal DiscordUserId} 로 받는다.
 * 토큰이 없거나 틀리면 아무것도 싣지 않고 넘긴다. 막는 일은 보안 설정의 경로 규칙이 한다.
 */
@RequiredArgsConstructor
public class DeviceTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticateDeviceUseCase authenticateDeviceUseCase;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<String> token = bearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (token.isPresent()) {
            AuthenticateDeviceQuery query = new AuthenticateDeviceQuery(token.get());
            Optional<DiscordUserId> userId = authenticateDeviceUseCase.authenticate(query);
            userId.ifPresent(DeviceTokenAuthenticationFilter::signIn);
        }
        chain.doFilter(request, response);
    }

    private static void signIn(DiscordUserId userId) {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(userId, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private static Optional<String> bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(QuickTtsApi.BEARER_PREFIX)) {
            return Optional.empty();
        }
        String token = authorization.substring(QuickTtsApi.BEARER_PREFIX.length()).strip();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }
}
