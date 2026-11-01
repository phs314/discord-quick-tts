package io.github.phs314.quicktts.bot.device.domain.vo;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;

/**
 * 기기 토큰의 SHA-256 해시 (소문자 16진수 64자). 서버는 토큰 원문 대신 이것만 저장하고 이것으로 기기를 찾는다.
 * 원문과 같은 {@code String} 이면 서로 바꿔 넣어도 컴파일러가 못 잡으므로 따로 감싼다.
 */
public record DeviceTokenHash(String value) {

    static final int LENGTH = 64;

    public DeviceTokenHash {
        if (value == null || value.length() != LENGTH
                || !value.chars().allMatch(c -> (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
            throw new InvalidDomainValueException("기기 토큰 해시 형식이 아닙니다.");
        }
    }
}
