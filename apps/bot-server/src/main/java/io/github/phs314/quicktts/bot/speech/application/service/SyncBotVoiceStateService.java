package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.port.in.SyncBotVoiceStateUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SyncBotVoiceStateCommand;
import io.github.phs314.quicktts.bot.speech.application.port.out.BotVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncBotVoiceStateService implements SyncBotVoiceStateUseCase {

    private final BotVoiceStatePort botVoiceStatePort;
    private final SpeechPlayerPort speechPlayerPort;

    @Override
    public void sync(SyncBotVoiceStateCommand command) {
        VoiceChannel from = command.from();
        botVoiceStatePort.withLockedState(from.guildId(), state -> {
            // 끊겼으면 남은 문장이 다음에 들어가는 채널에서 읽히지 않게 버린다.
            if (state.applyUpdate(from, command.to())) {
                speechPlayerPort.leave(from.guildId());
            }
        });
    }
}
