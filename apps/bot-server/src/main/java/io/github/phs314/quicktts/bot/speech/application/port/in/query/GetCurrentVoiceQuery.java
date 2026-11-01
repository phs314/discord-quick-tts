package io.github.phs314.quicktts.bot.speech.application.port.in.query;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;

/**
 * @param user 목소리를 볼 사용자
 */
public record GetCurrentVoiceQuery(DiscordUserId user) {
}
