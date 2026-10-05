package io.github.phs314.quicktts.bot.speech.application.service;

import io.github.phs314.quicktts.bot.speech.application.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayer;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizer;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocator;
import io.github.phs314.quicktts.bot.speech.domain.Speech;
import io.github.phs314.quicktts.bot.speech.domain.VoiceChannel;

public class QuickChatService implements SpeakQuickChatUseCase {

    private final VoiceChannelLocator voiceChannelLocator;
    private final SpeechSynthesizer speechSynthesizer;
    private final SpeechPlayer speechPlayer;

    public QuickChatService(VoiceChannelLocator voiceChannelLocator,
                            SpeechSynthesizer speechSynthesizer,
                            SpeechPlayer speechPlayer) {
        this.voiceChannelLocator = voiceChannelLocator;
        this.speechSynthesizer = speechSynthesizer;
        this.speechPlayer = speechPlayer;
    }

    @Override
    public void speak(SpeakQuickChatCommand command) {
        VoiceChannel channel = voiceChannelLocator.findCurrentChannel(command.speaker())
                .orElseThrow(() -> new SpeakerNotInVoiceChannelException(command.speaker()));

        Speech speech = speechSynthesizer.synthesize(command.message());
        speechPlayer.play(channel, speech);
    }
}
