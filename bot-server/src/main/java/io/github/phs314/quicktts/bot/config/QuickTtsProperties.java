package io.github.phs314.quicktts.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("quicktts")
public record QuickTtsProperties(Discord discord, Tts tts, GuildAllowlist guildAllowlist) {

    public QuickTtsProperties {
        if (discord == null || discord.token() == null || discord.token().isBlank()) {
            throw new IllegalStateException("DISCORD_TOKEN 이 비어 있습니다. 저장소의 .env, 사용자 폴더의 .discord-quick-tts/server.env, 환경변수 중 하나로 봇 토큰을 설정하세요.");
        }
        if (tts == null) {
            tts = new Tts(null, null);
        }
        if (guildAllowlist == null) {
            guildAllowlist = new GuildAllowlist(null);
        }
    }

    public record Discord(String token) {
    }

    /**
     * @param defaultVoice         목소리를 고르지 않은 사용자에게 쓰는 목소리
     * @param edgeChromiumVersion  Edge 목소리 서버가 확인하는 Edge 브라우저 버전
     */
    public record Tts(String defaultVoice, String edgeChromiumVersion) {

        public Tts {
            if (defaultVoice == null || defaultVoice.isBlank()) {
                defaultVoice = "edge:ko-KR-SunHiNeural";
            }
            if (edgeChromiumVersion == null || edgeChromiumVersion.isBlank()) {
                edgeChromiumVersion = "143.0.3650.75";
            }
        }
    }

    /**
     * @param enabled 봇 주인이 허용한 디스코드 서버에만 봇이 남게 할지. 누구나 초대할 수 있게 공개할 때 끈다. 기본은 켜짐.
     */
    public record GuildAllowlist(Boolean enabled) {

        public GuildAllowlist {
            if (enabled == null) {
                enabled = true;
            }
        }
    }
}
