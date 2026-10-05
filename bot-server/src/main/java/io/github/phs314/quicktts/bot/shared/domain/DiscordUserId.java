package io.github.phs314.quicktts.bot.shared.domain;

/**
 * 디스코드 사용자 ID (snowflake).
 */
public record DiscordUserId(long value) {

    public DiscordUserId {
        if (value <= 0) {
            throw new InvalidDomainValueException("디스코드 사용자 ID 는 양수여야 합니다.");
        }
    }

    public static DiscordUserId parse(String value) {
        try {
            return new DiscordUserId(Long.parseLong(value));
        } catch (NumberFormatException e) {
            throw new InvalidDomainValueException("디스코드 사용자 ID 형식이 아닙니다: " + value, e);
        }
    }
}
