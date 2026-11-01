package io.github.phs314.quicktts.bot.settings.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.settings.domain.UnknownVoiceException;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VoiceChoicesTest {

    private static final VoiceChoice SUNHI = new VoiceChoice(new VoiceId("edge:sunhi"), "선희");
    private static final VoiceChoice INJOON = new VoiceChoice(new VoiceId("edge:injoon"), "인준");
    private final VoiceChoices choices = new VoiceChoices(List.of(SUNHI, INJOON), SUNHI.id());

    @Test
    void 고른_목소리가_목록에_있으면_그_목소리로_읽는다() {
        assertThat(choices.chosenOrDefault(Optional.of(INJOON.id()))).isEqualTo(INJOON);
    }

    @Test
    void 고른_적이_없으면_기본_목소리로_읽는다() {
        assertThat(choices.chosenOrDefault(Optional.empty())).isEqualTo(SUNHI);
    }

    @Test
    void 골랐던_목소리가_목록에서_사라졌으면_기본_목소리로_읽는다() {
        assertThat(choices.chosenOrDefault(Optional.of(new VoiceId("edge:사라진-목소리")))).isEqualTo(SUNHI);
    }

    @Test
    void 목록에_있는_목소리만_고를_수_있다() {
        assertThat(choices.choose(INJOON.id())).isEqualTo(INJOON);
        assertThatThrownBy(() -> choices.choose(new VoiceId("edge:없는-목소리")))
                .isInstanceOf(UnknownVoiceException.class);
    }

    @Test
    void 기본_목소리는_목록_안에_있어야_한다() {
        assertThatThrownBy(() -> new VoiceChoices(List.of(INJOON), SUNHI.id()))
                .isInstanceOf(IllegalStateException.class);
    }
}
