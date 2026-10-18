package io.github.phs314.quicktts.bot;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.config.DiscordProperties;
import io.github.phs314.quicktts.bot.guild.adapter.out.settings.GuildAllowlistProperties;
import io.github.phs314.quicktts.bot.speech.adapter.out.tts.TtsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/** 설정 값 레코드가 허용 목록 기본값을 채우고, 필수 값이 비면 켜질 때 멈추는지 확인한다. */
class ConfigurationPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfig.class)
            .withPropertyValues("quicktts.discord.token=test-token",
                    "quicktts.tts.default-voice=edge:ko-KR-SunHiNeural",
                    "quicktts.tts.edge-chromium-version=143.0.3650.75");

    @Test
    void 허용_목록은_설정하지_않으면_켜져_있다() {
        runner.run(context -> assertThat(context.getBean(GuildAllowlistProperties.class).enabled()).isTrue());
    }

    @Test
    void 허용_목록은_설정으로_끌_수_있다() {
        runner.withPropertyValues("quicktts.guild-allowlist.enabled=false")
                .run(context -> assertThat(context.getBean(GuildAllowlistProperties.class).enabled()).isFalse());
    }

    @Test
    void 기본_목소리가_비어_있으면_켜지지_않고_고치는_방법을_알려준다() {
        runner.withPropertyValues("quicktts.tts.default-voice=")
                .run(context -> assertThat(context).hasFailed().getFailure()
                        .rootCause().hasMessageContaining("QUICKTTS_DEFAULT_VOICE 가 비어 있습니다"));
    }

    @Test
    void 디스코드_토큰이_비어_있으면_켜지지_않고_설정하는_방법을_알려준다() {
        runner.withPropertyValues("quicktts.discord.token=")
                .run(context -> assertThat(context).hasFailed().getFailure()
                        .rootCause().hasMessageContaining("봇 토큰을 설정하세요"));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({DiscordProperties.class, TtsProperties.class, GuildAllowlistProperties.class})
    static class PropertiesConfig {
    }
}
