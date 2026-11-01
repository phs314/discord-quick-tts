package io.github.phs314.quicktts.bot.speech.domain.vo;

/**
 * 음성 채널에 틀 음성.
 *
 * @param content 음성 파일 바이트. 읽기만 한다.
 * @param format  음성 형식 (예: {@code mp3})
 */
public record Audio(byte[] content, String format) {
}
