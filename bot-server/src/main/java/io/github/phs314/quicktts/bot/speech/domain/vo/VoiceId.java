package io.github.phs314.quicktts.bot.speech.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;

/**
 * 목소리 하나를 가리키는 식별자 (예: {@code edge:ko-KR-SunHiNeural}).
 * 어떤 엔진의 목소리인지는 TTS 어댑터만 알고, 도메인은 같은지만 비교한다.
 */
public record VoiceId(String value) {

    public VoiceId {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainValueException("목소리 식별자가 비어 있습니다.");
        }
    }
}
