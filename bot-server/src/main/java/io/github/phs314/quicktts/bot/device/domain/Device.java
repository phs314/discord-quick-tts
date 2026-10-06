package io.github.phs314.quicktts.bot.device.domain;

import io.github.phs314.quicktts.bot.device.domain.vo.DeviceId;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceName;
import io.github.phs314.quicktts.bot.device.domain.vo.DeviceToken;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import java.time.Instant;

/**
 * 디스코드 사용자에게 연결된 PC 한 대.
 *
 * @param id           사용자가 기기를 골라 해제할 때 쓰는 식별자
 * @param tokenHash    기기 토큰의 해시 ({@link DeviceToken#hash()})
 * @param owner        이 기기로 quick chat 을 보내는 사용자
 * @param name         사용자에게 보여 줄 PC 이름
 * @param registeredAt 등록 시각
 */
public record Device(DeviceId id, String tokenHash, DiscordUserId owner, DeviceName name, Instant registeredAt) {

    public static Device register(DeviceToken token, DiscordUserId owner, DeviceName name, Instant now) {
        return new Device(DeviceId.generate(), token.hash(), owner, name, now);
    }
}
