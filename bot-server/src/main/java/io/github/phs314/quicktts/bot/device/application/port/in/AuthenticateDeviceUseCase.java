package io.github.phs314.quicktts.bot.device.application.port.in;

import io.github.phs314.quicktts.bot.shared.domain.DiscordUserId;
import java.util.Optional;

/**
 * 기기 토큰으로 그 PC 의 주인을 찾는다. 다른 컨텍스트가 부르는 device 컨텍스트의 공개 입구라서
 * device 도메인 타입 대신 토큰 원문을 받는다.
 */
public interface AuthenticateDeviceUseCase {

    Optional<DiscordUserId> authenticate(String rawDeviceToken);
}
