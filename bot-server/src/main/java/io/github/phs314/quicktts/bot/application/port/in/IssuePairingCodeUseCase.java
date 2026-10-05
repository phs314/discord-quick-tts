package io.github.phs314.quicktts.bot.application.port.in;

import io.github.phs314.quicktts.bot.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.domain.device.Pairing;

/**
 * 디스코드 사용자에게 PC 를 연결할 일회용 코드를 발급한다.
 */
public interface IssuePairingCodeUseCase {

    Pairing issue(DiscordUserId owner);
}
