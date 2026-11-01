package io.github.phs314.quicktts.bot.guild.adapter.out.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 허용 목록 설정 ({@code quicktts.guild-allowlist}). 이 패키지의 설정 어댑터만 쓴다.
 *
 * @param enabled 봇 주인이 허용한 디스코드 서버에만 봇이 남게 할지. 누구나 초대할 수 있게 공개할 때 끈다. 기본은 켜짐.
 */
@ConfigurationProperties("quicktts.guild-allowlist")
public record GuildAllowlistProperties(@DefaultValue("true") boolean enabled) {
}
