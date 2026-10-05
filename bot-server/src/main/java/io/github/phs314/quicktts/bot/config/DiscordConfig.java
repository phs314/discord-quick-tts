package io.github.phs314.quicktts.bot.config;

import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfig {

    @Bean(destroyMethod = "shutdown")
    public JDA jda(QuickTtsProperties properties) throws InterruptedException {
        return JDABuilder.createDefault(properties.discord().token())
                // 사용자가 어느 음성 채널에 있는지 알아야 하므로 음성 상태를 캐시한다.
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .setAudioModuleConfig(new AudioModuleConfig()
                        .withDaveSessionFactory(new JDaveSessionFactory()))
                .build()
                .awaitReady();
    }
}
