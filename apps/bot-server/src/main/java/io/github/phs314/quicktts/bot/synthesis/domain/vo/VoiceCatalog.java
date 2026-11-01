package io.github.phs314.quicktts.bot.synthesis.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import java.util.List;

/**
 * 엔진들이 지금 낼 수 있는 목소리와 기본 목소리.
 *
 * @param voices       낼 수 있는 목소리
 * @param defaultVoice 목소리를 고르지 않았을 때 쓰는 목소리. {@code voices} 안에 있다.
 */
public record VoiceCatalog(List<Voice> voices, VoiceId defaultVoice) {

    public VoiceCatalog {
        voices = List.copyOf(voices);
        if (voices.stream().noneMatch(voice -> voice.id().equals(defaultVoice))) {
            throw new IllegalStateException("기본 목소리가 목소리 목록에 없습니다.");
        }
    }
}
