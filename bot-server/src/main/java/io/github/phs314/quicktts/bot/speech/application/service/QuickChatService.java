package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.port.in.FindMyVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.Optional;

public class QuickChatService implements SpeakQuickChatUseCase, FindMyVoiceChannelUseCase {

    private final VoiceChannelLocatorPort voiceChannelLocator;
    private final SpeechSynthesizerPort speechSynthesizer;
    private final SpeechPlayerPort speechPlayer;
    private final ManageVoiceUseCase voices;

    public QuickChatService(VoiceChannelLocatorPort voiceChannelLocator,
                            SpeechSynthesizerPort speechSynthesizer,
                            SpeechPlayerPort speechPlayer,
                            ManageVoiceUseCase voices) {
        this.voiceChannelLocator = voiceChannelLocator;
        this.speechSynthesizer = speechSynthesizer;
        this.speechPlayer = speechPlayer;
        this.voices = voices;
    }

    @Override
    public void speak(SpeakQuickChatCommand command) {
        VoiceChannel channel = voiceChannelLocator.findCurrentChannel(command.speaker())
                .map(VoiceChannelDetails::channel)
                .orElseThrow(() -> new SpeakerNotInVoiceChannelException(command.speaker()));

        VoiceId voice = voices.currentVoice(command.speaker()).id();
        Speech speech = speechSynthesizer.synthesize(command.message(), voice);
        speechPlayer.play(channel, speech);
    }

    @Override
    public Optional<VoiceChannelDetails> findMyVoiceChannel(DiscordUserId user) {
        return voiceChannelLocator.findCurrentChannel(user);
    }
}
