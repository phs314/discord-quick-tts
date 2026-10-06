package io.github.phs314.quicktts.bot.speech.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;

/**
 * 사용자가 quick chat 으로 보낸, 음성으로 읽어 줄 문장.
 * 앞뒤 공백은 지우고, 비어 있거나 {@link #MAX_LENGTH} 자를 넘으면 만들 수 없다.
 */
public record QuickChatMessage(String text) {

    public static final int MAX_LENGTH = 200;

    public QuickChatMessage {
        if (text == null || text.isBlank()) {
            throw new InvalidDomainValueException("문장이 비어 있습니다.");
        }
        text = text.strip();
        if (text.length() > MAX_LENGTH) {
            throw new InvalidDomainValueException("문장은 " + MAX_LENGTH + "자를 넘을 수 없습니다.");
        }
    }
}
