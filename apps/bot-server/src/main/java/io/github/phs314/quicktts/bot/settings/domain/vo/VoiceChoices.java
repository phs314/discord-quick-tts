package io.github.phs314.quicktts.bot.settings.domain.vo;

import io.github.phs314.quicktts.bot.settings.domain.UnknownVoiceException;
import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import java.util.List;
import java.util.Optional;

/**
 * 지금 고를 수 있는 목소리들과 기본 목소리.
 *
 * @param choices        고를 수 있는 목소리
 * @param defaultVoiceId 목소리를 고른 적이 없거나 고른 목소리가 더 이상 없을 때 쓰는 목소리. {@code choices} 안에 있다.
 */
public record VoiceChoices(List<VoiceChoice> choices, VoiceId defaultVoiceId) {

    public VoiceChoices {
        choices = List.copyOf(choices);
        if (choices.stream().noneMatch(choice -> choice.id().equals(defaultVoiceId))) {
            throw new IllegalStateException("기본 목소리가 목소리 목록에 없습니다.");
        }
    }

    public Optional<VoiceChoice> find(VoiceId id) {
        return choices.stream().filter(choice -> choice.id().equals(id)).findFirst();
    }

    /**
     * 사용자가 새로 고를 목소리. 목록에 있는 목소리만 고를 수 있다.
     *
     * @throws UnknownVoiceException 목록에 없는 목소리일 때
     */
    public VoiceChoice choose(VoiceId id) {
        return find(id).orElseThrow(() -> new UnknownVoiceException(id));
    }

    /** 사용자가 고른 목소리로 읽을 목소리를 정한다. 고른 적이 없거나 목록에 없는 목소리면 기본 목소리. */
    public VoiceChoice chosenOrDefault(Optional<VoiceId> chosen) {
        return chosen.flatMap(this::find).orElseGet(() -> find(defaultVoiceId).orElseThrow());
    }
}
