package io.github.phs314.quicktts.bot.device.application.port.in.usecase;

import io.github.phs314.quicktts.bot.device.application.port.in.dto.IssuedPairingCodeDto;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * 디스코드 사용자에게 PC 를 연결할 일회용 코드를 발급한다.
 */
public interface IssuePairingCodeUseCase {

    IssuedPairingCodeDto issue(DiscordUserId owner);
}
