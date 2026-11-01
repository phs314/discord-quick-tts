package io.github.phs314.quicktts.bot.synthesis.domain.vo;

/**
 * 엔진이 문장을 읽어 만든 음성.
 *
 * @param content 음성 파일 바이트. 캐시가 여러 곳에 함께 내주므로 읽기만 한다.
 * @param format  음성 형식 (예: {@code mp3})
 */
public record Audio(byte[] content, String format) {
}
