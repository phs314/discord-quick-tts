package io.github.phs314.quicktts.bot.domain;

/**
 * 디스코드 서버(길드) 안의 음성 채널 하나를 가리킨다.
 */
public record VoiceChannel(long guildId, long channelId) {
}
