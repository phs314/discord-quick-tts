package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.exception.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.VoiceChannelInUseException;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.BotSeatPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.domain.BotSeat;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpeakQuickChatService implements SpeakQuickChatUseCase {

    private final VoiceChannelLocatorPort voiceChannelLocator;
    private final SpeechSynthesizerPort speechSynthesizer;
    private final SpeechPlayerPort speechPlayer;
    private final BotSeatPort botSeats;
    private final ManageVoiceUseCase voices;

    @Override
    public void speak(SpeakQuickChatCommand command) {
        VoiceChannel channel = voiceChannelLocator.findCurrentChannel(command.speaker())
                .map(VoiceChannelDetails::channel)
                .orElseThrow(() -> new SpeakerNotInVoiceChannelException(command.speaker()));

        VoiceId voice = voices.currentVoice(command.speaker()).id();
        Speech speech = speechSynthesizer.synthesize(command.message(), voice);

        // 같은 디스코드 서버에서는 자리 잡기와 재생 순서 넣기가 한 번에 하나씩 일어나서 다른 채널의 요청이 끼어들지 못한다.
        botSeats.withSeat(channel.guildId(), seat -> {
            if (!seat.isFreeFor(channel, voiceChannelLocator::hasPeople)) {
                throw inUse(seat);
            }
            // 들어가지 못하면(권한 없음 등) 예외가 나서 자리는 그대로 남는다.
            speechPlayer.join(channel);
            seat.sitIn(channel, voiceChannelLocator::hasPeople);
            speechPlayer.play(channel.guildId(), speech);
        });
    }

    private VoiceChannelInUseException inUse(BotSeat seat) {
        VoiceChannel inUse = seat.channel().orElseThrow();
        return new VoiceChannelInUseException(voiceChannelLocator.findDetails(inUse)
                .orElseGet(() -> new VoiceChannelDetails(inUse, "", null, "알 수 없음")));
    }
}
