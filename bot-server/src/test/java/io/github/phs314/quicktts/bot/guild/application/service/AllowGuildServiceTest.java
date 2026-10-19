package io.github.phs314.quicktts.bot.guild.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.AllowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeAllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class AllowGuildServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");
    private static final DiscordUserId OWNER = new DiscordUserId(100L);
    private static final DiscordUserId SOMEONE = new DiscordUserId(200L);
    private static final GuildId MINE = new GuildId(1L);
    private static final GuildId FRIEND = new GuildId(2L);

    private final FakeAllowedGuildPort allowedGuilds = new FakeAllowedGuildPort();
    private final AllowGuildService service =
            new AllowGuildService(() -> OWNER, allowedGuilds, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void 봇_주인은_서버를_넣을_수_있고_이미_있으면_false() {
        assertThat(service.allow(new AllowGuildCommand(OWNER, FRIEND))).isTrue();
        assertThat(service.allow(new AllowGuildCommand(OWNER, FRIEND))).isFalse();

        assertThat(allowedGuilds.findAll()).containsExactly(AllowedGuild.allow(FRIEND, NOW));
    }

    @Test
    void 봇_주인이_아니면_넣을_수_없다() {
        assertThatThrownBy(() -> service.allow(new AllowGuildCommand(SOMEONE, FRIEND)))
                .isInstanceOf(NotBotOwnerException.class);
        assertThat(allowedGuilds.findAll()).isEmpty();
    }
}
