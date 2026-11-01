package io.github.phs314.quicktts.bot.config;

import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import java.time.Duration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 디스코드 게이트웨이 연결. 샤드 수는 디스코드가 권하는 대로 정하므로 지금은 1개이고,
 * 봇이 들어간 디스코드 서버가 늘면 코드를 바꾸지 않고 샤드가 늘어난다 (ADR 0046).
 */
@Configuration
public class DiscordConfig {

    /** 디스코드는 샤드를 5초에 하나씩만 붙게 하므로, 모든 샤드가 붙었는지 이 간격으로 본다. */
    private static final Duration SHARD_CHECK_INTERVAL = Duration.ofMillis(200);

    @Bean(destroyMethod = "shutdown")
    public ShardManager shardManager(DiscordProperties properties) throws InterruptedException {
        // 봇은 음성 상태만 쓰므로 그 소식만 받는다. 채팅 메시지 같은 다른 소식은 구독하지 않는다.
        // 디스코드 서버 들어감·나감 소식은 JDA 가 늘 받고, 슬래시 명령과 메뉴는 인텐트 없이 온다.
        ShardManager shardManager = DefaultShardManagerBuilder.createLight(properties.token(), GatewayIntent.GUILD_VOICE_STATES)
                // 사용자가 어느 음성 채널에 있는지 알아야 하므로 음성 상태를 캐시한다.
                .enableCache(CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                // 봇에게 따로 건 채널 권한도 알아야 들어갈 수 없는 음성 채널과 글을 쓸 수 없는 안내 채널을 미리 가린다.
                .enableCache(CacheFlag.MEMBER_OVERRIDES)
                .setAudioModuleConfig(new AudioModuleConfig()
                        .withDaveSessionFactory(new JDaveSessionFactory()))
                .build();
        awaitAllShardsReady(shardManager);
        return shardManager;
    }

    /**
     * 켜질 때 하는 일(봇 주인 받아 오기, 꺼져 있는 동안 초대된 서버 확인, 슬래시 명령 등록)은 디스코드 서버 캐시가 다 차 있어야 하므로,
     * 모든 샤드가 준비될 때까지 기다린 뒤 빈을 내준다.
     */
    private static void awaitAllShardsReady(ShardManager shardManager) throws InterruptedException {
        while (shardManager.getShardsQueued() > 0 || shardManager.getShardCache().size() < shardManager.getShardsTotal()) {
            Thread.sleep(SHARD_CHECK_INTERVAL);
        }
        for (JDA shard : shardManager.getShards()) {
            shard.awaitReady();
        }
    }
}
