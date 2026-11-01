package io.github.phs314.quicktts.bot.guild.application.service;

import io.github.phs314.quicktts.bot.guild.application.port.in.SeedAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistSettingPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.JoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.guild.domain.vo.GuildAllowlist;
import java.time.Clock;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeedAllowlistService implements SeedAllowlistUseCase {

    private final AllowedGuildPort allowedGuildPort;
    private final JoinedGuildPort joinedGuildPort;
    private final GuildAllowlistSettingPort guildAllowlistSettingPort;
    private final Clock clock;

    /**
     * 봇이 들어가 있는 디스코드 서버를 한 트랜잭션으로 넣는다. 일부만 남으면 다음에 켤 때 목록이 비어 있지 않아서
     * 나머지 디스코드 서버에서 나가게 되기 때문이다. 디스코드 서버 나가기는 이 트랜잭션에 넣지 않고
     * {@link LeaveAllNotAllowedGuildsService} 에서 따로 한다 (ADR 0033).
     */
    @Override
    @Transactional
    public void seedIfEmpty() {
        if (!currentAllowlist().needsSeeding()) {
            return;
        }
        Instant now = clock.instant();
        allowedGuildPort.saveAll(joinedGuildPort.joinedGuilds().stream()
                .map(guildId -> AllowedGuild.allow(guildId, now))
                .toList());
    }

    private GuildAllowlist currentAllowlist() {
        return new GuildAllowlist(guildAllowlistSettingPort.isEnabled(),
                allowedGuildPort.findAll().stream().map(AllowedGuild::guildId).collect(Collectors.toSet()));
    }
}
