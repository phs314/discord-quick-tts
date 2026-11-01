package io.github.phs314.quicktts.bot.guild.application.port.out;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * 봇 주인을 알려 준다. 허용 목록은 봇 주인만 바꿀 수 있다.
 */
public interface BotOwnerPort {

    DiscordUserId owner();
}
