package io.github.phs314.quicktts.bot.guild.domain.event;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;

/**
 * 봇 주인이 디스코드 서버를 허용 목록에서 뺐다. 허용 목록에서 빼는 트랜잭션이 커밋된 뒤에 받는 쪽이
 * 봇을 그 서버에서 내보낸다 (ADR 0033).
 *
 * @param guildId 허용 목록에서 뺀 디스코드 서버
 */
public record GuildRemovedFromAllowlist(GuildId guildId) {
}
