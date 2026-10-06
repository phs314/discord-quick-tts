package io.github.phs314.quicktts.bot.speech.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.UnknownVoiceException;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechSynthesizer;
import io.github.phs314.quicktts.bot.speech.application.port.out.VoicePreferenceRepository;
import io.github.phs314.quicktts.bot.speech.domain.vo.QuickChatMessage;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.Voice;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VoiceServiceTest {

    static final Voice SUNHI = new Voice(new VoiceId("edge:sunhi"), "선희");
    static final Voice INJOON = new Voice(new VoiceId("edge:injoon"), "인준");
    private static final DiscordUserId USER = new DiscordUserId(42L);

    private final InMemoryVoicePreferences preferences = new InMemoryVoicePreferences();
    private final VoiceService service = new VoiceService(new FakeSynthesizer(), preferences);

    @Test
    void 고른_적이_없으면_기본_목소리다() {
        assertThat(service.currentVoice(USER)).isEqualTo(SUNHI);
    }

    @Test
    void 목소리를_바꾸면_다음부터_그_목소리다() {
        service.changeVoice(USER, INJOON.id());

        assertThat(service.currentVoice(USER)).isEqualTo(INJOON);
    }

    @Test
    void 목록에_없는_목소리로는_바꿀_수_없다() {
        assertThatThrownBy(() -> service.changeVoice(USER, new VoiceId("edge:없음")))
                .isInstanceOf(UnknownVoiceException.class);
        assertThat(preferences.byUser).isEmpty();
    }

    @Test
    void 골랐던_목소리가_목록에서_사라졌으면_기본_목소리로_읽는다() {
        preferences.save(USER, new VoiceId("edge:사라진-목소리"));

        assertThat(service.currentVoice(USER)).isEqualTo(SUNHI);
    }

    static class FakeSynthesizer implements SpeechSynthesizer {

        @Override
        public List<Voice> voices() {
            return List.of(SUNHI, INJOON);
        }

        @Override
        public VoiceId defaultVoice() {
            return SUNHI.id();
        }

        @Override
        public Speech synthesize(QuickChatMessage message, VoiceId voice) {
            return new Speech((voice.value() + ":" + message.text()).getBytes(), "txt");
        }
    }

    static class InMemoryVoicePreferences implements VoicePreferenceRepository {

        final Map<DiscordUserId, VoiceId> byUser = new HashMap<>();

        @Override
        public Optional<VoiceId> find(DiscordUserId user) {
            return Optional.ofNullable(byUser.get(user));
        }

        @Override
        public void save(DiscordUserId user, VoiceId voice) {
            byUser.put(user, voice);
        }
    }
}
