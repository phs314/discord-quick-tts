package io.github.phs314.quicktts.bot.guild.application.port.in.usecase;

import io.github.phs314.quicktts.bot.guild.application.port.in.command.AllowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.DisallowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.GuildAllowlistDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.query.GetAllowlistQuery;

/**
 * 봇 주인이 봇을 들일 디스코드 서버 허용 목록을 보고 바꾼다.
 * 모든 메서드는 봇 주인이 아니면
 * {@link io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException} 을 던진다.
 */
public interface ManageGuildAllowlistUseCase {

    GuildAllowlistDto getAllowlist(GetAllowlistQuery query);

    /** @return 새로 넣었으면 true, 이미 있었으면 false */
    boolean allow(AllowGuildCommand command);

    /**
     * 허용 목록에서 뺀다. 허용 목록을 쓰는 중이고 봇이 그 디스코드 서버에 있으면 안내를 남기고 나간다.
     *
     * @return 목록에서 뺐으면 true, 원래 없었으면 false
     */
    boolean disallow(DisallowGuildCommand command);
}
