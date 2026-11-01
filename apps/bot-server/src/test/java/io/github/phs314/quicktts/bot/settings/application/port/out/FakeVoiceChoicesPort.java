package io.github.phs314.quicktts.bot.settings.application.port.out;

import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoice;
import io.github.phs314.quicktts.bot.settings.domain.vo.VoiceChoices;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import java.util.List;

/** synthesis 에 목소리를 묻는 대신 쓰는 대역. 선희와 인준이 있고 기본은 선희다. */
public class FakeVoiceChoicesPort implements VoiceChoicesPort {

    public static final VoiceChoice SUNHI = new VoiceChoice(new VoiceId("fake:sunhi"), "선희");
    public static final VoiceChoice INJOON = new VoiceChoice(new VoiceId("fake:injoon"), "인준");

    @Override
    public VoiceChoices getVoiceChoices() {
        return new VoiceChoices(List.of(SUNHI, INJOON), SUNHI.id());
    }
}
