package io.github.phs314.quicktts.bot.speech.domain.vo;

/**
 * 사용자가 고를 수 있는 목소리.
 *
 * @param id    목소리 식별자
 * @param label 사용자에게 보여 줄 이름 (예: "선희 (여성)")
 */
public record Voice(VoiceId id, String label) {
}
