package io.github.phs314.quicktts.bot.guild.adapter.out.discord;

import io.github.phs314.quicktts.bot.guild.application.port.out.BotOwnerPort;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.stereotype.Component;

/**
 * 봇 주인은 Discord Developer Portal 에서 이 봇 앱을 만든 사람이다. 앱이 팀 소유면 팀 주인이다.
 * 봇 서버가 켜질 때 디스코드에서 한 번 받아 와 기억한다. 슬래시 명령을 받는 스레드에서 디스코드를 기다리지 않게 하려는 것이다.
 */
@Component
@RequiredArgsConstructor
public class BotOwnerDiscordAdapter implements BotOwnerPort {

    private final ShardManager shardManager;
    private DiscordUserId owner;

    @PostConstruct
    void load() {
        ApplicationInfo info = shardManager.retrieveApplicationInfo().complete();
        owner = new DiscordUserId(info.getTeam() != null ? info.getTeam().getOwnerIdLong() : info.getOwner().getIdLong());
    }

    @Override
    public DiscordUserId owner() {
        return owner;
    }
}
