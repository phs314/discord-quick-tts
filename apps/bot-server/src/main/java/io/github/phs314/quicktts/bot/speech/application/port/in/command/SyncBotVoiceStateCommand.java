package io.github.phs314.quicktts.bot.speech.application.port.in.command;

import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import java.util.Optional;

/**
 * @param from 봇이 있던 음성 채널
 * @param to 봇이 옮겨진 음성 채널. 끊겼으면 빈 값
 */
public record SyncBotVoiceStateCommand(VoiceChannel from, Optional<VoiceChannel> to) {
}
