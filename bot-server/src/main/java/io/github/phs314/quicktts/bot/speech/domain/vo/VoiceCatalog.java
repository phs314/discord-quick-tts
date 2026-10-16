package io.github.phs314.quicktts.bot.speech.domain.vo;

import io.github.phs314.quicktts.bot.speech.domain.UnknownVoiceException;
import java.util.List;
import java.util.Optional;

/**
 * 지금 고를 수 있는 목소리들과 기본 목소리.
 *
 * @param voices       고를 수 있는 목소리
 * @param defaultVoice 목소리를 고른 적이 없거나 고른 목소리가 더 이상 없을 때 쓰는 목소리. {@code voices} 안에 있다.
 */
public record VoiceCatalog(List<Voice> voices, VoiceId defaultVoice) {

    public VoiceCatalog {
        voices = List.copyOf(voices);
        if (voices.stream().noneMatch(voice -> voice.id().equals(defaultVoice))) {
            throw new IllegalStateException("기본 목소리가 목소리 목록에 없습니다.");
        }
    }

    public Optional<Voice> find(VoiceId id) {
        return voices.stream().filter(voice -> voice.id().equals(id)).findFirst();
    }

    /**
     * 사용자가 새로 고를 목소리. 목록에 있는 목소리만 고를 수 있다.
     *
     * @throws UnknownVoiceException 목록에 없는 목소리일 때
     */
    public Voice choose(VoiceId id) {
        return find(id).orElseThrow(() -> new UnknownVoiceException(id));
    }

    /** 사용자가 고른 목소리로 읽을 목소리를 정한다. 고른 적이 없거나 목록에 없는 목소리면 기본 목소리. */
    public Voice voiceFor(Optional<VoiceId> chosen) {
        return chosen.flatMap(this::find).orElseGet(() -> find(defaultVoice).orElseThrow());
    }
}
