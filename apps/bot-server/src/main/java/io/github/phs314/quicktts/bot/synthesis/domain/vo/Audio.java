package io.github.phs314.quicktts.bot.synthesis.domain.vo;

import java.util.Objects;

/**
 * 엔진이 문장을 읽어 만든 음성.
 *
 * @param content 음성 파일 바이트
 * @param format  음성 형식 (예: {@code mp3})
 */
public record Audio(byte[] content, String format) {

    /** 배열은 바뀔 수 있어서 받을 때와 내줄 때 복사한다. 캐시가 같은 음성을 여러 곳에 내줘도 서로 건드리지 않는다. */
    public Audio {
        content = Objects.requireNonNull(content).clone();
        Objects.requireNonNull(format);
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
