package io.github.phs314.quicktts.bot.guild.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.guild.application.port.out.FakeAllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeGuildAllowlistConfigPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeJoinedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SeedAllowlistServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");
    private static final GuildId MINE = new GuildId(1L);
    private static final GuildId STRANGER = new GuildId(3L);

    private final FakeAllowedGuildPort allowedGuilds = new FakeAllowedGuildPort();
    private final FakeJoinedGuildPort joinedGuilds = new FakeJoinedGuildPort(MINE, STRANGER);
    private final FakeGuildAllowlistConfigPort setting = new FakeGuildAllowlistConfigPort();
    private final SeedAllowlistService service = new SeedAllowlistService(
            allowedGuilds, joinedGuilds, setting, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void 허용_목록이_비어_있으면_지금_있는_서버를_모두_허용한다() {
        service.seedIfEmpty();

        assertThat(allowedGuilds.findAll()).extracting(AllowedGuild::guildId).containsExactly(MINE, STRANGER);
        assertThat(joinedGuilds.left()).isEmpty();
    }

    @Test
    void 허용_목록이_있으면_다시_채우지_않는다() {
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));

        service.seedIfEmpty();

        assertThat(allowedGuilds.findAll()).containsExactly(AllowedGuild.allow(MINE, NOW));
    }

    @Test
    void 허용_목록이_꺼져_있으면_채우지_않는다() {
        setting.setEnabled(false);

        service.seedIfEmpty();

        assertThat(allowedGuilds.findAll()).isEmpty();
    }
}
