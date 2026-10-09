package io.github.phs314.quicktts.bot.speech.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;

/**
 * 디스코드 서버(길드) ID (snowflake). 디스코드 사용자 ID 와 바꿔 넣지 않도록 따로 감싼다.
 */
public record GuildId(long value) {

    public GuildId {
        if (value <= 0) {
            throw new InvalidDomainValueException("디스코드 서버 ID 는 양수여야 합니다.");
        }
    }
}
