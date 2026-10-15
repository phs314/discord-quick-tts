package io.github.phs314.quicktts.bot.speech.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VoiceCatalogTest {

    private static final Voice SUNHI = new Voice(new VoiceId("edge:sunhi"), "선희");
    private static final Voice INJOON = new Voice(new VoiceId("edge:injoon"), "인준");
    private final VoiceCatalog catalog = new VoiceCatalog(List.of(SUNHI, INJOON), SUNHI.id());

    @Test
    void 고른_목소리가_목록에_있으면_그_목소리로_읽는다() {
        assertThat(catalog.voiceFor(Optional.of(INJOON.id()))).isEqualTo(INJOON);
    }

    @Test
    void 고른_적이_없으면_기본_목소리로_읽는다() {
        assertThat(catalog.voiceFor(Optional.empty())).isEqualTo(SUNHI);
    }

    @Test
    void 골랐던_목소리가_목록에서_사라졌으면_기본_목소리로_읽는다() {
        assertThat(catalog.voiceFor(Optional.of(new VoiceId("edge:사라진-목소리")))).isEqualTo(SUNHI);
    }

    @Test
    void 기본_목소리는_목록_안에_있어야_한다() {
        assertThatThrownBy(() -> new VoiceCatalog(List.of(INJOON), SUNHI.id()))
                .isInstanceOf(IllegalStateException.class);
    }
}
