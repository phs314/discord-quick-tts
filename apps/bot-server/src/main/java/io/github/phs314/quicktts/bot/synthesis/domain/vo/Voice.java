package io.github.phs314.quicktts.bot.synthesis.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;

/**
 * 엔진이 낼 수 있는 목소리.
 *
 * @param id    목소리 식별자
 * @param label 사용자에게 보여 줄 이름 (예: "선희 (여성)")
 */
public record Voice(VoiceId id, String label) {
}
