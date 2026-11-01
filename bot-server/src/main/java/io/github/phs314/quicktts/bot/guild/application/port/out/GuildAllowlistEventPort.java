package io.github.phs314.quicktts.bot.guild.application.port.out;

import io.github.phs314.quicktts.bot.guild.domain.event.GuildRemovedFromAllowlist;

/** 허용 목록에서 일어난 일을 알린다. 받는 쪽은 트랜잭션이 커밋된 뒤에 받는다 (ADR 0026, 0033). */
public interface GuildAllowlistEventPort {

    void publish(GuildRemovedFromAllowlist event);
}
