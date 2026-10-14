package io.github.phs314.quicktts.bot.guild.application.service;

import io.github.phs314.quicktts.bot.guild.application.port.in.usecase.EnforceGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.out.AllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.GuildAllowlistSettingPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.JoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnforceGuildAllowlistService implements EnforceGuildAllowlistUseCase {

    private final AllowedGuildPort allowedGuilds;
    private final JoinedGuildPort joinedGuilds;
    private final GuildAllowlistSettingPort setting;
    private final Clock clock;

    @Override
    public void checkJoinedGuild(GuildId guild) {
        if (setting.enabled() && !allowedGuilds.exists(guild)) {
            joinedGuilds.leaveNotAllowed(guild);
        }
    }

    /**
     * 처음 채울 때는 디스코드 서버마다 한 번씩 DB 에 쓰므로 한 트랜잭션으로 묶는다 (ADR 0009).
     * 중간에 실패해 일부만 남으면 다음에 켤 때 목록이 비어 있지 않아서 나머지 디스코드 서버에서 나가게 된다.
     */
    @Override
    @Transactional
    public void checkAllJoinedGuilds() {
        if (!setting.enabled()) {
            return;
        }
        List<GuildId> joined = joinedGuilds.joinedGuilds();
        if (allowedGuilds.findAll().isEmpty()) {
            // 허용 목록을 처음 켰을 때 봇 주인이 이미 쓰던 디스코드 서버에서 쫓겨나지 않게 한다.
            joined.forEach(guild -> allowedGuilds.add(AllowedGuild.allow(guild, clock.instant())));
            return;
        }
        joined.forEach(this::checkJoinedGuild);
    }
}
