package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.ChangeVoiceCommand;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeSpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeVoicePreferencePort;
import io.github.phs314.quicktts.bot.speech.domain.UnknownVoiceException;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import org.junit.jupiter.api.Test;

class ChangeVoiceServiceTest {

    private static final DiscordUserId USER = new DiscordUserId(42L);

    private final FakeVoicePreferencePort preferences = new FakeVoicePreferencePort();
    private final ChangeVoiceService service = new ChangeVoiceService(new FakeSpeechSynthesizerPort(), preferences);

    @Test
    void 바꾼_목소리를_돌려주고_저장한다() {
        assertThat(service.changeVoice(new ChangeVoiceCommand(USER, FakeSpeechSynthesizerPort.INJOON.id())))
                .isEqualTo(FakeSpeechSynthesizerPort.INJOON);
        assertThat(preferences.findVoice(USER)).contains(FakeSpeechSynthesizerPort.INJOON.id());
    }

    @Test
    void 목록에_없는_목소리로는_바꿀_수_없다() {
        assertThatThrownBy(() -> service.changeVoice(new ChangeVoiceCommand(USER, new VoiceId("fake:없음"))))
                .isInstanceOf(UnknownVoiceException.class);
        assertThat(preferences.saved()).isEmpty();
    }
}
