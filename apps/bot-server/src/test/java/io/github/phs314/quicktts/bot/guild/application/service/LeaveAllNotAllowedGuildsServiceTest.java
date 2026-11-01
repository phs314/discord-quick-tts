package io.github.phs314.quicktts.bot.guild.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.guild.application.port.out.FakeAllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeGuildAllowlistConfigPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeJoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LeaveAllNotAllowedGuildsServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");
    private static final GuildId MINE = new GuildId(1L);
    private static final GuildId STRANGER = new GuildId(3L);

    private final FakeAllowedGuildPort allowedGuilds = new FakeAllowedGuildPort();
    private final FakeJoinedGuildPort joinedGuilds = new FakeJoinedGuildPort(MINE, STRANGER);
    private final FakeGuildAllowlistConfigPort setting = new FakeGuildAllowlistConfigPort();
    private final LeaveAllNotAllowedGuildsService service =
            new LeaveAllNotAllowedGuildsService(allowedGuilds, joinedGuilds, setting);

    @Test
    void 켜질_때_꺼져_있는_동안_초대된_서버에서_나간다() {
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));

        service.leaveAllNotAllowed();

        assertThat(joinedGuilds.left()).containsExactly(STRANGER);
        assertThat(joinedGuilds.joinedGuilds()).containsExactly(MINE);
    }

    @Test
    void 채우기_전에는_켜질_때_아무_데서도_나가지_않는다() {
        service.leaveAllNotAllowed();

        assertThat(joinedGuilds.left()).isEmpty();
        assertThat(allowedGuilds.findAll()).isEmpty();
    }

    @Test
    void 허용_목록이_꺼져_있으면_어디든_남는다() {
        setting.setEnabled(false);
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));

        service.leaveAllNotAllowed();

        assertThat(joinedGuilds.left()).isEmpty();
    }
}
