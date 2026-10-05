package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.shared.domain.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.port.in.FindMyVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayer;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizer;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocator;
import io.github.phs314.quicktts.bot.speech.domain.Speech;
import io.github.phs314.quicktts.bot.speech.domain.VoiceChannel;
import io.github.phs314.quicktts.bot.speech.domain.VoiceChannelDetails;
import io.github.phs314.quicktts.bot.speech.domain.VoiceId;
import java.util.Optional;

public class QuickChatService implements SpeakQuickChatUseCase, FindMyVoiceChannelUseCase {

    private final VoiceChannelLocator voiceChannelLocator;
    private final SpeechSynthesizer speechSynthesizer;
    private final SpeechPlayer speechPlayer;
    private final ManageVoiceUseCase voices;

    public QuickChatService(VoiceChannelLocator voiceChannelLocator,
                            SpeechSynthesizer speechSynthesizer,
                            SpeechPlayer speechPlayer,
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
