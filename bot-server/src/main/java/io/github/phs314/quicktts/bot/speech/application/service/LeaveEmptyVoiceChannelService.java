package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.port.in.LeaveEmptyVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.LeaveEmptyVoiceChannelCommand;
import io.github.phs314.quicktts.bot.speech.application.port.out.BotVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceStatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveEmptyVoiceChannelService implements LeaveEmptyVoiceChannelUseCase {

    private final BotVoiceStatePort botVoiceStatePort;
    private final VoiceStatePort voiceStatePort;
    private final SpeechPlayerPort speechPlayerPort;

    @Override
    public void leaveIfEmpty(LeaveEmptyVoiceChannelCommand command) {
        GuildId guildId = command.guildId();
        botVoiceStatePort.withLockedState(guildId, state -> {
            if (state.leaveIfEmpty(voiceStatePort::hasHumanMembers)) {
                speechPlayerPort.leave(guildId);
            }
        });
    }
}
