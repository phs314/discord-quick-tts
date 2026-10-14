package io.github.phs314.quicktts.bot.shared.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;

/**
 * 디스코드 사용자 ID (snowflake).
 */
public record DiscordUserId(long value) {

    public DiscordUserId {
        if (value <= 0) {
            throw new InvalidDomainValueException("디스코드 사용자 ID 는 양수여야 합니다.");
        }
    }
}
