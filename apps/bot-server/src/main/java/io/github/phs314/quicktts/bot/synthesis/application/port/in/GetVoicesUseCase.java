package io.github.phs314.quicktts.bot.synthesis.application.port.in;

import io.github.phs314.quicktts.bot.synthesis.application.port.in.dto.VoiceCatalogDto;

/**
 * 엔진들이 낼 수 있는 목소리 목록과 기본 목소리를 본다. 사용자 설정(settings)이 부른다.
 */
public interface GetVoicesUseCase {

    VoiceCatalogDto getVoices();
}
