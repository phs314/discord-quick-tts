package io.github.phs314.quicktts.bot.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 디스코드 봇 설정 ({@code quicktts.discord}). JDA 를 만드는 {@link DiscordConfig} 만 쓴다.
 * 다른 설정은 그 설정을 쓰는 어댑터 패키지에 둔다 (TTS 는 {@code TtsProperties}, 허용 목록은 {@code GuildAllowlistProperties}).
 * 토큰이 비어 있으면 켜질 때 멈춘다.
 *
 * @param token 디스코드 봇 토큰
 */
@Validated
@ConfigurationProperties("quicktts.discord")
public record DiscordProperties(
        @NotBlank(message = "DISCORD_TOKEN 이 비어 있습니다. 저장소의 .env, 사용자 폴더의 .discord-quick-tts/server.env, "
                + "환경변수 중 하나로 봇 토큰을 설정하세요.")
        String token) {
}
