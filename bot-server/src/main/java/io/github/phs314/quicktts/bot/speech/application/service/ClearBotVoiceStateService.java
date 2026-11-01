package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.ClearBotVoiceStateUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.ClearBotVoiceStateCommand;
import io.github.phs314.quicktts.bot.speech.application.port.out.BotVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClearBotVoiceStateService implements ClearBotVoiceStateUseCase {

    private final BotVoiceStatePort botVoiceStatePort;
    private final SpeechPlayerPort speechPlayerPort;

    @Override
    public void clear(ClearBotVoiceStateCommand command) {
        GuildId guildId = command.guildId();
        botVoiceStatePort.withLockedState(guildId, state -> {
            state.clear();
            speechPlayerPort.forget(guildId);
        });
    }
}
