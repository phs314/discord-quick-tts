package io.github.phs314.quicktts.bot.guild.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.guild.application.port.in.command.LeaveNotAllowedGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeAllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeGuildAllowlistConfigPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeJoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LeaveNotAllowedGuildServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");
    private static final GuildId MINE = new GuildId(1L);
    private static final GuildId FRIEND = new GuildId(2L);
    private static final GuildId STRANGER = new GuildId(3L);

    private final FakeAllowedGuildPort allowedGuilds = new FakeAllowedGuildPort();
    private final FakeJoinedGuildPort joinedGuilds = new FakeJoinedGuildPort(MINE, STRANGER);
    private final FakeGuildAllowlistConfigPort setting = new FakeGuildAllowlistConfigPort();
    private final LeaveNotAllowedGuildService service =
            new LeaveNotAllowedGuildService(allowedGuilds, joinedGuilds, setting);

    @Test
    void 허용_목록에_없는_서버에_초대되면_나간다() {
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));

        service.leaveIfNotAllowed(new LeaveNotAllowedGuildCommand(STRANGER));

        assertThat(joinedGuilds.left()).containsExactly(STRANGER);
    }

    @Test
    void 허용한_서버에_초대되면_남는다() {
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(FRIEND, NOW));
        joinedGuilds.join(FRIEND);

        service.leaveIfNotAllowed(new LeaveNotAllowedGuildCommand(FRIEND));

        assertThat(joinedGuilds.left()).isEmpty();
    }

    @Test
    void 허용_목록이_꺼져_있으면_어디든_남는다() {
        setting.setEnabled(false);
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));

        service.leaveIfNotAllowed(new LeaveNotAllowedGuildCommand(STRANGER));

        assertThat(joinedGuilds.left()).isEmpty();
    }
}
