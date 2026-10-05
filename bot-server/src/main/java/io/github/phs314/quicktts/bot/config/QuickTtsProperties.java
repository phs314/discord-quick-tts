package io.github.phs314.quicktts.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("quicktts")
public record QuickTtsProperties(Discord discord, String apiKey, Tts tts) {

    public QuickTtsProperties {
        if (discord == null || isBlank(discord.token())) {
            throw new IllegalStateException("DISCORD_TOKEN 이 비어 있습니다. .env 또는 환경변수로 봇 토큰을 설정하세요.");
        }
        if (isBlank(apiKey)) {
            throw new IllegalStateException("QUICKTTS_API_KEY 가 비어 있습니다. .env 또는 환경변수로 클라이언트 API 키를 설정하세요.");
        }
        if (tts == null) {
            tts = new Tts("google-translate", "ko");
        }
    }

    public record Discord(String token) {
    }

    public record Tts(String engine, String language) {
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
