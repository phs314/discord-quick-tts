package io.github.phs314.quicktts.bot.config;

import io.github.phs314.quicktts.bot.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.application.port.out.SpeechPlayer;
import io.github.phs314.quicktts.bot.application.port.out.SpeechSynthesizer;
import io.github.phs314.quicktts.bot.application.port.out.VoiceChannelLocator;
import io.github.phs314.quicktts.bot.application.service.QuickChatService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 애플리케이션 계층은 스프링을 모르게 두고, 여기서 포트와 어댑터를 이어 붙인다.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public SpeakQuickChatUseCase speakQuickChatUseCase(VoiceChannelLocator voiceChannelLocator,
                                                       SpeechSynthesizer speechSynthesizer,
                                                       SpeechPlayer speechPlayer) {
        return new QuickChatService(voiceChannelLocator, speechSynthesizer, speechPlayer);
    }
}
