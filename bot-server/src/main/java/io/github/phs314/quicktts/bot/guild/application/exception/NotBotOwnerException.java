package io.github.phs314.quicktts.bot.guild.application.exception;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * 봇 주인이 아닌 사람이 허용 목록을 보거나 바꾸려 할 때 던진다.
 */
public class NotBotOwnerException extends RuntimeException {

    public NotBotOwnerException(DiscordUserId requester) {
        super("봇 주인만 허용 목록을 다룰 수 있습니다: " + requester.value());
    }
}
