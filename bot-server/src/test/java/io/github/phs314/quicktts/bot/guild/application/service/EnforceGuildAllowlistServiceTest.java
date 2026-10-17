package io.github.phs314.quicktts.bot.guild.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.guild.application.port.out.FakeAllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeGuildAllowlistSettingPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeJoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class EnforceGuildAllowlistServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");
    private static final GuildId MINE = new GuildId(1L);
    private static final GuildId FRIEND = new GuildId(2L);
    private static final GuildId STRANGER = new GuildId(3L);

    private final FakeAllowedGuildPort allowedGuilds = new FakeAllowedGuildPort();
    private final FakeJoinedGuildPort joinedGuilds = new FakeJoinedGuildPort(MINE, STRANGER);
    private final FakeGuildAllowlistSettingPort setting = new FakeGuildAllowlistSettingPort();
    private final EnforceGuildAllowlistService service = new EnforceGuildAllowlistService(
            allowedGuilds, joinedGuilds, setting, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void 허용_목록에_없는_서버에_초대되면_나간다() {
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));

        service.leaveIfNotAllowed(STRANGER);

        assertThat(joinedGuilds.left()).containsExactly(STRANGER);
    }

    @Test
    void 허용한_서버에_초대되면_남는다() {
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(FRIEND, NOW));
        joinedGuilds.join(FRIEND);

        service.leaveIfNotAllowed(FRIEND);

        assertThat(joinedGuilds.left()).isEmpty();
    }

    @Test
    void 허용_목록이_꺼져_있으면_어디든_남는다() {
        setting.setEnabled(false);
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));

        service.leaveIfNotAllowed(STRANGER);
        service.leaveAllNotAllowed();

        assertThat(joinedGuilds.left()).isEmpty();
        assertThat(allowedGuilds.findAll()).containsExactly(AllowedGuild.allow(MINE, NOW));
    }

    @Test
    void 켜질_때_꺼져_있는_동안_초대된_서버에서_나간다() {
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));

        service.leaveAllNotAllowed();

        assertThat(joinedGuilds.left()).containsExactly(STRANGER);
        assertThat(joinedGuilds.joinedGuilds()).containsExactly(MINE);
    }

    @Test
    void 허용_목록이_비어_있으면_지금_있는_서버를_모두_허용한다() {
        service.leaveAllNotAllowed();

        assertThat(joinedGuilds.left()).isEmpty();
        assertThat(allowedGuilds.findAll()).extracting(AllowedGuild::guildId).containsExactly(MINE, STRANGER);
    }
}
