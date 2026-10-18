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
    public JDA jda(DiscordProperties properties) throws InterruptedException {
        // 봇은 음성 상태만 쓰므로 그 소식만 받는다. 채팅 메시지 같은 다른 소식은 구독하지 않는다.
        // 디스코드 서버 들어감·나감 소식은 JDA 가 늘 받고, 슬래시 명령과 메뉴는 인텐트 없이 온다.
        return JDABuilder.createLight(properties.token(), GatewayIntent.GUILD_VOICE_STATES)
                // 사용자가 어느 음성 채널에 있는지 알아야 하므로 음성 상태를 캐시한다.
                .enableCache(CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                // 봇에게 따로 건 채널 권한도 알아야 들어갈 수 없는 음성 채널과 글을 쓸 수 없는 안내 채널을 미리 가린다.
                .enableCache(CacheFlag.MEMBER_OVERRIDES)
                .setAudioModuleConfig(new AudioModuleConfig()
                        .withDaveSessionFactory(new JDaveSessionFactory()))
                .build()
                .awaitReady();
    }
}
