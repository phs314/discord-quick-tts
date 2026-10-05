package io.github.phs314.quicktts.bot.application.service;

import io.github.phs314.quicktts.bot.application.SpeakerNotInVoiceChannelException;
import io.github.phs314.quicktts.bot.application.port.in.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.application.port.out.SpeechPlayer;
import io.github.phs314.quicktts.bot.application.port.out.SpeechSynthesizer;
import io.github.phs314.quicktts.bot.application.port.out.VoiceChannelLocator;
import io.github.phs314.quicktts.bot.domain.Speech;
import io.github.phs314.quicktts.bot.domain.VoiceChannel;

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
