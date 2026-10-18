package io.github.phs314.quicktts.bot.device.application.port.in.usecase;

import io.github.phs314.quicktts.bot.device.application.port.in.query.AuthenticateDeviceQuery;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.util.Optional;

/**
 * 기기 토큰으로 그 PC 의 주인을 찾는다. 웹 요청마다 기기 토큰 인증 필터가 부른다 (ADR 0031).
 * 토큰이 없거나 틀리면 예외 대신 빈 값을 돌려주고, 막는 일은 보안 설정이 한다 ({@link AuthenticateDeviceQuery}).
 */
public interface AuthenticateDeviceUseCase {

    Optional<DiscordUserId> authenticate(AuthenticateDeviceQuery query);
}
