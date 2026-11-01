package io.github.phs314.quicktts.bot.synthesis.application.port.in.dto;

import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;
import io.github.phs314.quicktts.bot.synthesis.domain.vo.VoiceCatalog;
import java.util.List;

/**
 * 다른 컨텍스트에 보여 줄 목소리 목록과 기본 목소리.
 *
 * @param voices         고를 수 있는 목소리
 * @param defaultVoiceId 목소리를 고르지 않았을 때 쓰는 목소리
 */
public record VoiceCatalogDto(List<VoiceDto> voices, VoiceId defaultVoiceId) {

    public static VoiceCatalogDto from(VoiceCatalog catalog) {
        return new VoiceCatalogDto(catalog.voices().stream().map(VoiceDto::from).toList(), catalog.defaultVoice());
    }
}
