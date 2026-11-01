package io.github.phs314.quicktts.bot.synthesis.application.port.in.dto;

import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import io.github.phs314.quicktts.bot.synthesis.domain.vo.Voice;

/**
 * 다른 컨텍스트에 보여 줄 목소리 하나.
 *
 * @param id    목소리 식별자
 * @param label 사용자에게 보여 줄 이름
 */
public record VoiceDto(VoiceId id, String label) {

    public static VoiceDto from(Voice voice) {
        return new VoiceDto(voice.id(), voice.label());
    }
}
