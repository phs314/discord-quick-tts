package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.exception.AuthorNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.VoiceChannelInUseException;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.out.BotVoiceStatePort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceStatePort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferencePort;
import io.github.phs314.quicktts.bot.speech.domain.BotVoiceState;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.SpokenText;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceCatalog;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpeakQuickChatService implements SpeakQuickChatUseCase {

    private final VoiceStatePort voiceStatePort;
    private final SpeechSynthesizerPort speechSynthesizerPort;
    private final SpeechPlayerPort speechPlayerPort;
    private final BotVoiceStatePort botVoiceStatePort;
    private final VoicePreferencePort voicePreferencePort;

    @Override
    public void speak(SpeakQuickChatCommand command) {
        SpokenText text = command.message().toSpokenText();
        VoiceChannel channel = voiceStatePort.findUserVoiceChannel(command.author())
                .map(VoiceChannelDetails::channel)
                .orElseThrow(() -> new AuthorNotInVoiceChannelException(command.author()));

        VoiceCatalog catalog = new VoiceCatalog(speechSynthesizerPort.voices(), speechSynthesizerPort.defaultVoice());
        VoiceId voiceId = catalog.chosenOrDefault(voicePreferencePort.findVoice(command.author())).id();
        Speech speech = speechSynthesizerPort.synthesize(text, voiceId);

        // 같은 디스코드 서버에서는 채널 정하기와 재생 순서 넣기가 한 번에 하나씩 일어나서 다른 채널의 요청이 끼어들지 못한다.
        botVoiceStatePort.withLockedState(channel.guildId(), state -> {
            if (!state.canJoin(channel, voiceStatePort::hasHumanMembers)) {
                throw inUseException(state);
            }
            // 들어가지 못하면(권한 없음 등) 예외가 나서 상태는 그대로 남는다.
            speechPlayerPort.join(channel);
            state.join(channel, voiceStatePort::hasHumanMembers);
            speechPlayerPort.play(channel.guildId(), speech);
        });
    }

    private VoiceChannelInUseException inUseException(BotVoiceState state) {
        VoiceChannel inUse = state.channel().orElseThrow();
        return new VoiceChannelInUseException(voiceStatePort.findChannelDetails(inUse)
                .orElseGet(() -> new VoiceChannelDetails(inUse, "", null, "알 수 없음")));
    }
}
