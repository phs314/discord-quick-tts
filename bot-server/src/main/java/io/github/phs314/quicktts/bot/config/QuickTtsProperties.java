package io.github.phs314.quicktts.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("quicktts")
public record QuickTtsProperties(Discord discord, Tts tts) {

    public QuickTtsProperties {
        if (discord == null || discord.token() == null || discord.token().isBlank()) {
            throw new IllegalStateException("DISCORD_TOKEN 이 비어 있습니다. .env 또는 환경변수로 봇 토큰을 설정하세요.");
        }
        if (tts == null) {
            tts = new Tts("google-translate", "ko");
        }
    }

    public record Discord(String token) {
    }

    public record Tts(String engine, String language) {
    }
}
