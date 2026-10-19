package io.github.phs314.quicktts.bot.guild.application.port.in;

import io.github.phs314.quicktts.bot.guild.application.port.in.command.DisallowGuildCommand;

/**
 * 봇 주인이 디스코드 서버를 허용 목록에서 뺀다. 봇 주인이 아니면
 * {@link io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException} 을 던진다.
 */
public interface DisallowGuildUseCase {

    /**
     * 허용 목록에서 뺀다. 뺐으면 이 트랜잭션이 커밋된 뒤
     * {@link io.github.phs314.quicktts.bot.guild.domain.event.GuildDisallowed} 를 받는 쪽이 봇을 그 디스코드 서버에서
     * 내보내므로, 부르는 쪽은 나가기를 따로 부르지 않는다. 나가기는 되돌릴 수 없어서 트랜잭션 밖에서 한다 (ADR 0033).
     *
     * @return 목록에서 뺐으면 true, 원래 없었으면 false
     */
    boolean disallow(DisallowGuildCommand command);
}
