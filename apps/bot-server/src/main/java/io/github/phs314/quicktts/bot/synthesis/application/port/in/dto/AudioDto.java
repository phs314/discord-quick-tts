package io.github.phs314.quicktts.bot.synthesis.application.port.in.dto;

import io.github.phs314.quicktts.bot.synthesis.domain.vo.Audio;

/**
 * 다른 컨텍스트에 내주는 음성.
 *
 * @param content 음성 파일 바이트. 읽기만 한다.
 * @param format  음성 형식 (예: {@code mp3})
 */
public record AudioDto(byte[] content, String format) {

    public static AudioDto from(Audio audio) {
        return new AudioDto(audio.content(), audio.format());
    }
}
