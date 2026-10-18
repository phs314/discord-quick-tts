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
     * 허용 목록에서 뺀다. 봇을 그 디스코드 서버에서 내보내는 일은 하지 않으므로, 부르는 쪽이 이어서
     * {@link EnforceGuildAllowlistUseCase#leaveIfNotAllowed} 를 부른다. 나가기를 이 트랜잭션 밖에서 하기 위해서다
     * (ADR 0033).
     *
     * @return 목록에서 뺐으면 true, 원래 없었으면 false
     */
    boolean disallow(DisallowGuildCommand command);
}
