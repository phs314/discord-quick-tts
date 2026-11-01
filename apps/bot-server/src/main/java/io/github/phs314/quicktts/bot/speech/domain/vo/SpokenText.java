package io.github.phs314.quicktts.bot.speech.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;

/**
 * 문장을 소리 내 읽기 좋게 다듬은 글. TTS 엔진에는 이 글만 간다.
 * 다듬는 규칙은 {@link io.github.phs314.quicktts.bot.speech.domain.normalization.TextNormalizer} 에 있고, 다듬고 나서 비어 있으면 만들 수 없다.
 */
public record SpokenText(String text) {

    public SpokenText {
        if (text == null || text.isBlank()) {
            throw new InvalidDomainValueException("읽을 내용이 없습니다.");
        }
    }
}
