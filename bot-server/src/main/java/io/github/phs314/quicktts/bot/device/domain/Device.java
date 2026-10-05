package io.github.phs314.quicktts.bot.device.domain;

import io.github.phs314.quicktts.bot.shared.domain.DiscordUserId;
import java.time.Instant;

/**
 * 디스코드 사용자에게 연결된 PC 한 대.
 *
 * @param tokenHash    기기 토큰의 해시 ({@link DeviceToken#hash()})
 * @param owner        이 기기로 quick chat 을 보내는 사용자
 * @param registeredAt 등록 시각
 */
public record Device(String tokenHash, DiscordUserId owner, Instant registeredAt) {
}
