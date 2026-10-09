package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.exception.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.VoiceChannelInUseException;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.usecase.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
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
    private final ManageVoiceUseCase voices;

    @Override
    public void speak(SpeakQuickChatCommand command) {
        VoiceChannel channel = voiceChannelLocator.findCurrentChannel(command.speaker())
                .map(VoiceChannelDetails::channel)
                .orElseThrow(() -> new SpeakerNotInVoiceChannelException(command.speaker()));

        VoiceId voice = voices.currentVoice(command.speaker()).id();
        Speech speech = speechSynthesizer.synthesize(command.message(), voice);

        // 확인과 재생 사이에 다른 채널의 요청이 끼어들어 봇을 옮기지 않게 한 번에 하나씩 처리한다.
        // play 는 재생 순서에 넣기만 하고 바로 돌아오므로 오래 막지 않는다.
        synchronized (this) {
            rejectIfInUseElsewhere(channel);
            speechPlayer.play(channel, speech);
        }
    }

    /** 먼저 온 채널 우선: 봇이 같은 디스코드 서버의 다른 채널에서 쓰이고 있으면 옮겨 가지 않는다. */
    private void rejectIfInUseElsewhere(VoiceChannel channel) {
        voiceChannelLocator.findBotChannelInUse(channel.guildId())
                .filter(inUse -> !inUse.channel().equals(channel))
                .ifPresent(inUse -> {
                    throw new VoiceChannelInUseException(inUse);
                });
    }
}
