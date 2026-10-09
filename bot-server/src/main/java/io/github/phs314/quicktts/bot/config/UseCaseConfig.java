package io.github.phs314.quicktts.bot.config;

import io.github.phs314.quicktts.bot.device.application.port.in.AuthenticateDeviceUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.IssuePairingCodeUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.ManageOwnDevicesUseCase;
import io.github.phs314.quicktts.bot.device.application.port.in.RegisterDeviceUseCase;
import io.github.phs314.quicktts.bot.device.application.port.out.DevicePort;
import io.github.phs314.quicktts.bot.device.application.port.out.PairingPort;
import io.github.phs314.quicktts.bot.device.application.service.AuthenticateDeviceService;
import io.github.phs314.quicktts.bot.device.application.service.IssuePairingCodeService;
import io.github.phs314.quicktts.bot.device.application.service.ManageOwnDevicesService;
import io.github.phs314.quicktts.bot.device.application.service.RegisterDeviceService;
import io.github.phs314.quicktts.bot.speech.application.port.in.FindMyVoiceChannelUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.ManageVoiceUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.in.SpeakQuickChatUseCase;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoiceChannelLocatorPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferencePort;
import io.github.phs314.quicktts.bot.speech.application.service.FindMyVoiceChannelService;
import io.github.phs314.quicktts.bot.speech.application.service.ManageVoiceService;
import io.github.phs314.quicktts.bot.speech.application.service.SpeakQuickChatService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 애플리케이션 계층은 스프링을 모르게 두고, 여기서 포트와 어댑터를 이어 붙인다.
 * 유스케이스(입력 포트) 하나에 서비스 하나씩 등록한다 (ADR 0008).
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
        return new ManageVoiceService(speechSynthesizer, voicePreferencePort);
    }

    @Bean
    public SpeakQuickChatUseCase speakQuickChatUseCase(VoiceChannelLocatorPort voiceChannelLocator,
                                                       SpeechSynthesizerPort speechSynthesizer,
                                                       SpeechPlayerPort speechPlayer,
                                                       ManageVoiceUseCase manageVoiceUseCase) {
        return new SpeakQuickChatService(voiceChannelLocator, speechSynthesizer, speechPlayer, manageVoiceUseCase);
    }

    @Bean
    public FindMyVoiceChannelUseCase findMyVoiceChannelUseCase(VoiceChannelLocatorPort voiceChannelLocator) {
        return new FindMyVoiceChannelService(voiceChannelLocator);
    }

    @Bean
    public IssuePairingCodeUseCase issuePairingCodeUseCase(PairingPort pairingPort, Clock clock) {
        return new IssuePairingCodeService(pairingPort, clock);
    }

    @Bean
    public RegisterDeviceUseCase registerDeviceUseCase(PairingPort pairingPort, DevicePort devicePort, Clock clock) {
        return new RegisterDeviceService(pairingPort, devicePort, clock);
    }

    @Bean
    public AuthenticateDeviceUseCase authenticateDeviceUseCase(DevicePort devicePort) {
        return new AuthenticateDeviceService(devicePort);
    }

    @Bean
    public ManageOwnDevicesUseCase manageOwnDevicesUseCase(DevicePort devicePort) {
        return new ManageOwnDevicesService(devicePort);
    }
}
