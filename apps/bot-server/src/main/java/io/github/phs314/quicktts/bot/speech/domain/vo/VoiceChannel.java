package io.github.phs314.quicktts.bot.speech.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;

/**
 * 디스코드 서버(길드) 안의 음성 채널 하나를 가리킨다.
 */
public record VoiceChannel(GuildId guildId, long channelId) {
}
