package io.github.phs314.quicktts.bot.speech.application.port.in.query;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * @param user 음성 채널을 찾을 사용자
 */
public record FindCurrentUserVoiceChannelQuery(DiscordUserId user) {
}
