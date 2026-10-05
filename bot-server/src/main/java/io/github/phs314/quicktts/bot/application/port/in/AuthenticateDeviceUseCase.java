package io.github.phs314.quicktts.bot.application.port.in;

import io.github.phs314.quicktts.bot.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.domain.device.DeviceToken;
import java.util.Optional;

/**
 * 기기 토큰으로 그 PC 의 주인을 찾는다.
 */
public interface AuthenticateDeviceUseCase {

    Optional<DiscordUserId> authenticate(DeviceToken token);
}
