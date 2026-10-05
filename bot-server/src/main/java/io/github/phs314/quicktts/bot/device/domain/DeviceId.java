package io.github.phs314.quicktts.bot.device.domain;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import java.util.UUID;

/**
 * 기기 식별자. 토큰과 달리 비밀이 아니라서 디스코드 메뉴 등에 그대로 실어도 된다.
 */
public record DeviceId(String value) {

    public DeviceId {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainValueException("기기 식별자가 비어 있습니다.");
        }
    }

    public static DeviceId generate() {
        return new DeviceId(UUID.randomUUID().toString());
    }
}
