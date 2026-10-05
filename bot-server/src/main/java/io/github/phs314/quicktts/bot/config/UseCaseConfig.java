package io.github.phs314.quicktts.bot.config;

import io.github.phs314.quicktts.bot.device.application.port.out.DeviceRepository;
import io.github.phs314.quicktts.bot.device.application.port.out.PairingRepository;
import io.github.phs314.quicktts.bot.device.application.service.DeviceRegistrationService;
import io.github.phs314.quicktts.bot.speech.application.port.in.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayer;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizer;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocator;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferenceRepository;
import io.github.phs314.quicktts.bot.speech.application.service.QuickChatService;
import io.github.phs314.quicktts.bot.speech.application.service.VoiceService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 애플리케이션 계층은 스프링을 모르게 두고, 여기서 포트와 어댑터를 이어 붙인다.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ManageVoiceUseCase manageVoiceUseCase(SpeechSynthesizer speechSynthesizer,
                                                 VoicePreferenceRepository voicePreferenceRepository) {
        return new VoiceService(speechSynthesizer, voicePreferenceRepository);
    }

    @Bean
    public SpeakQuickChatUseCase speakQuickChatUseCase(VoiceChannelLocator voiceChannelLocator,
                                                       SpeechSynthesizer speechSynthesizer,
                                                       SpeechPlayer speechPlayer,
                                                       ManageVoiceUseCase manageVoiceUseCase) {
        return new QuickChatService(voiceChannelLocator, speechSynthesizer, speechPlayer, manageVoiceUseCase);
    }

    /** 연결 코드 발급, 기기 등록, 기기 인증 유스케이스를 함께 맡는다. */
    @Bean
    public DeviceRegistrationService deviceRegistrationService(PairingRepository pairingRepository,
                                                               DeviceRepository deviceRepository,
                                                               Clock clock) {
        return new DeviceRegistrationService(pairingRepository, deviceRepository, clock);
    }
}
