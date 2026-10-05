package io.github.phs314.quicktts.bot.application.port.in;

import io.github.phs314.quicktts.bot.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.domain.QuickChatMessage;

/**
 * @param speaker 문장을 보낸 사용자
 * @param message 읽어 줄 문장
 */
public record SpeakQuickChatCommand(DiscordUserId speaker, QuickChatMessage message) {
}
