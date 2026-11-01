package io.github.phs314.quicktts.bot.guild.application.exception;

import io.github.phs314.quicktts.bot.shared.domain.BusinessException;
import io.github.phs314.quicktts.bot.shared.domain.ErrorKind;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.api.ApiErrorCode;

/**
 * 봇 주인이 아닌 사람이 허용 목록을 보거나 바꾸려 할 때 던진다.
 */
public class NotBotOwnerException extends BusinessException {

    public NotBotOwnerException(DiscordUserId requester) {
        super(ErrorKind.PERMISSION_DENIED, ApiErrorCode.NOT_BOT_OWNER, "봇 주인만 허용 목록을 다룰 수 있습니다: " + requester.value());
    }
}
