package io.github.phs314.quicktts.bot.config;

import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.device.application.port.out.PairingPort;
import io.github.phs314.quicktts.bot.device.application.service.DeviceRegistrationService;
import io.github.phs314.quicktts.bot.speech.application.port.in.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferencePort;
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
    public ManageVoiceUseCase manageVoiceUseCase(SpeechSynthesizerPort speechSynthesizer,
                                                 VoicePreferencePort voicePreferencePort) {
        return new VoiceService(speechSynthesizer, voicePreferencePort);
    }

    /** quick chat 읽어 주기와 "어느 채널에서 읽힐지" 조회 유스케이스를 함께 맡는다. */
    @Bean
    public QuickChatService quickChatService(VoiceChannelLocatorPort voiceChannelLocator,
                                             SpeechSynthesizerPort speechSynthesizer,
                                             SpeechPlayerPort speechPlayer,
                                             ManageVoiceUseCase manageVoiceUseCase) {
        return new QuickChatService(voiceChannelLocator, speechSynthesizer, speechPlayer, manageVoiceUseCase);
    }

    /** 연결 코드 발급, 기기 등록, 기기 인증 유스케이스를 함께 맡는다. */
    @Bean
    public DeviceRegistrationService deviceRegistrationService(PairingPort pairingPort,
                                                               DevicePort devicePort,
                                                               Clock clock) {
        return new DeviceRegistrationService(pairingPort, devicePort, clock);
    }
}
