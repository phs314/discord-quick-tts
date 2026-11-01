package io.github.phs314.quicktts.bot.guild.application.port.in;

import io.github.phs314.quicktts.bot.guild.application.port.in.command.AddToAllowlistCommand;

/**
 * 봇 주인이 디스코드 서버를 허용 목록에 넣는다. 봇 주인이 아니면
 * {@link io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException} 을 던진다.
 */
public interface AddToAllowlistUseCase {

    /** @return 새로 넣었으면 true, 이미 있었으면 false */
    boolean add(AddToAllowlistCommand command);
}
