package io.github.phs314.quicktts.bot.settings.application.port.in.command;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;

/**
 * @param user 목소리를 바꿀 사용자
 * @param voiceId 새로 고른 목소리
 */
public record ChangeVoiceCommand(DiscordUserId user, VoiceId voiceId) {
}
