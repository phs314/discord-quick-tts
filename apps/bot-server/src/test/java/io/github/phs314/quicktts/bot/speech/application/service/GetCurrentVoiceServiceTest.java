package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.ChangeVoiceCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.query.GetCurrentVoiceQuery;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeSpeechSynthesizerPort;
import io.github.phs314.quicktts.bot.speech.application.port.out.FakeVoicePreferencePort;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import org.junit.jupiter.api.Test;

class GetCurrentVoiceServiceTest {

    private static final DiscordUserId USER = new DiscordUserId(42L);

    private final FakeSpeechSynthesizerPort synthesizer = new FakeSpeechSynthesizerPort();
    private final FakeVoicePreferencePort preferences = new FakeVoicePreferencePort();
    private final GetCurrentVoiceService service = new GetCurrentVoiceService(synthesizer, preferences);

    @Test
    void 고른_적이_없으면_기본_목소리다() {
        assertThat(service.getCurrentVoice(new GetCurrentVoiceQuery(USER))).isEqualTo(FakeSpeechSynthesizerPort.SUNHI);
    }

    @Test
    void 목소리를_바꾸면_다음부터_그_목소리다() {
        new ChangeVoiceService(synthesizer, preferences)
                .changeVoice(new ChangeVoiceCommand(USER, FakeSpeechSynthesizerPort.INJOON.id()));

        assertThat(service.getCurrentVoice(new GetCurrentVoiceQuery(USER))).isEqualTo(FakeSpeechSynthesizerPort.INJOON);
    }

    @Test
    void 골랐던_목소리가_목록에서_사라졌으면_기본_목소리로_읽는다() {
        preferences.saveVoice(USER, new VoiceId("fake:사라진-목소리"));

        assertThat(service.getCurrentVoice(new GetCurrentVoiceQuery(USER))).isEqualTo(FakeSpeechSynthesizerPort.SUNHI);
    }
}
