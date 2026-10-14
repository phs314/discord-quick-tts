package io.github.phs314.quicktts.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 디스코드 봇 설정 ({@code quicktts.discord}). JDA 를 만드는 {@link DiscordConfig} 만 쓴다.
 * 다른 설정은 그 설정을 쓰는 어댑터 패키지에 둔다 (TTS 는 {@code TtsProperties}, 허용 목록은 {@code GuildAllowlistProperties}).
 *
 * @param token 디스코드 봇 토큰
 */
@ConfigurationProperties("quicktts.discord")
public record DiscordProperties(String token) {

    public DiscordProperties {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("DISCORD_TOKEN 이 비어 있습니다. 저장소의 .env, 사용자 폴더의 .discord-quick-tts/server.env, 환경변수 중 하나로 봇 토큰을 설정하세요.");
        }
    }
}
