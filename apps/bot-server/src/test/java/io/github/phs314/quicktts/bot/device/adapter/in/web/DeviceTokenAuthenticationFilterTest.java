package io.github.phs314.quicktts.bot.device.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.device.application.port.in.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.api.QuickTtsApi;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 기기 토큰으로 연결한 사용자를 알아내 스프링 시큐리티에 싣는지, 토큰이 맞지 않으면 아무것도 싣지 않고 넘기는지 확인한다.
 */
class DeviceTokenAuthenticationFilterTest {

    private static final String TOKEN = "good-token";
    private static final DiscordUserId USER = new DiscordUserId(42L);
    private static final AuthenticateDeviceUseCase AUTHENTICATE =
            query -> query.rawDeviceToken().equals(TOKEN) ? Optional.of(USER) : Optional.empty();

    private final DeviceTokenAuthenticationFilter filter = new DeviceTokenAuthenticationFilter(AUTHENTICATE);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 맞는_기기_토큰이면_연결한_사용자를_싣는다() throws Exception {
        MockFilterChain chain = filter(QuickTtsApi.BEARER_PREFIX + TOKEN);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(USER);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void 헤더가_없으면_싣지_않고_넘긴다() throws Exception {
        assertNotSignedInButPassedOn(null);
    }

    @Test
    void Bearer_가_아니면_싣지_않고_넘긴다() throws Exception {
        assertNotSignedInButPassedOn("Basic " + TOKEN);
    }

    @Test
    void 토큰이_비었으면_싣지_않고_넘긴다() throws Exception {
        assertNotSignedInButPassedOn(QuickTtsApi.BEARER_PREFIX + " ");
    }

    @Test
    void 해제된_기기의_토큰이면_싣지_않고_넘긴다() throws Exception {
        assertNotSignedInButPassedOn(QuickTtsApi.BEARER_PREFIX + "revoked-token");
    }

    private void assertNotSignedInButPassedOn(String authorization) throws Exception {
        MockFilterChain chain = filter(authorization);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    private MockFilterChain filter(String authorization) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (authorization != null) {
            request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        }
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return chain;
    }
}
