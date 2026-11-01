package io.github.phs314.quicktts.bot.synthesis.application.port.in.command;

import io.github.phs314.quicktts.bot.shared.domain.vo.VoiceId;

/**
 * @param text    소리 내 읽을 꼴로 다듬은 문장
 * @param voiceId 읽을 목소리
 */
public record SynthesizeSpeechCommand(String text, VoiceId voiceId) {
}
