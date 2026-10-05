package io.github.phs314.quicktts.bot.speech.domain;

/**
 * 문장을 음성으로 만든 결과.
 *
 * @param audio         음성 파일 바이트
 * @param fileExtension 파일 형식을 나타내는 확장자 (예: {@code mp3})
 */
public record Speech(byte[] audio, String fileExtension) {
}
