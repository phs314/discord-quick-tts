package io.github.phs314.quicktts.bot.settings.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;

/**
 * 사용자가 고를 수 있는 목소리 하나. {@code /목소리} 명령의 선택지(choice) 하나가 된다.
 *
 * @param id    목소리 식별자
 * @param label 사용자에게 보여 줄 이름 (예: "선희 (여성)")
 */
public record VoiceChoice(VoiceId id, String label) {
}
