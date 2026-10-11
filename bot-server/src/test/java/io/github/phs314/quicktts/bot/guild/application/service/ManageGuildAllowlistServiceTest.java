package io.github.phs314.quicktts.bot.guild.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.AllowedGuildDto;
import io.github.phs314.quicktts.bot.guild.application.service.FakeGuildPorts.FixedSetting;
import io.github.phs314.quicktts.bot.guild.application.service.FakeGuildPorts.InMemoryAllowedGuilds;
import io.github.phs314.quicktts.bot.guild.application.service.FakeGuildPorts.RecordingJoinedGuilds;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ManageGuildAllowlistServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");
    private static final DiscordUserId OWNER = new DiscordUserId(100L);
    private static final DiscordUserId SOMEONE = new DiscordUserId(200L);
    private static final GuildId MINE = new GuildId(1L);
    private static final GuildId FRIEND = new GuildId(2L);

    private final InMemoryAllowedGuilds allowedGuilds = new InMemoryAllowedGuilds();
    private final RecordingJoinedGuilds joinedGuilds = new RecordingJoinedGuilds(MINE);
    private final FixedSetting setting = new FixedSetting();
    private final ManageGuildAllowlistService service = new ManageGuildAllowlistService(
            () -> OWNER, allowedGuilds, joinedGuilds, setting, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void 봇_주인은_서버를_넣고_볼_수_있다() {
        assertThat(service.allow(OWNER, FRIEND)).isTrue();
        assertThat(service.allow(OWNER, FRIEND)).isFalse();

        var allowlist = service.allowlist(OWNER);
        assertThat(allowlist.enabled()).isTrue();
        assertThat(allowlist.guilds()).containsExactly(new AllowedGuildDto(FRIEND, NOW));
    }

    @Test
    void 봇_주인이_아니면_아무것도_할_수_없다() {
        assertThatThrownBy(() -> service.allow(SOMEONE, FRIEND)).isInstanceOf(NotBotOwnerException.class);
        assertThatThrownBy(() -> service.disallow(SOMEONE, MINE)).isInstanceOf(NotBotOwnerException.class);
        assertThatThrownBy(() -> service.allowlist(SOMEONE)).isInstanceOf(NotBotOwnerException.class);
        assertThat(allowedGuilds.byGuild).isEmpty();
        assertThat(joinedGuilds.left).isEmpty();
    }

    @Test
    void 목록에서_빼면_봇이_그_서버에서_나간다() {
        service.allow(OWNER, MINE);

        assertThat(service.disallow(OWNER, MINE)).isTrue();

        assertThat(allowedGuilds.byGuild).isEmpty();
        assertThat(joinedGuilds.left).containsExactly(MINE);
    }

    @Test
    void 봇이_없는_서버를_빼면_목록에서만_빠진다() {
        service.allow(OWNER, FRIEND);

        assertThat(service.disallow(OWNER, FRIEND)).isTrue();
        assertThat(service.disallow(OWNER, FRIEND)).isFalse();

        assertThat(joinedGuilds.left).isEmpty();
    }

    @Test
    void 허용_목록이_꺼져_있으면_빼도_나가지_않는다() {
        setting.enabled = false;
        service.allow(OWNER, MINE);

        service.disallow(OWNER, MINE);

        assertThat(joinedGuilds.left).isEmpty();
        assertThat(service.allowlist(OWNER).enabled()).isFalse();
    }
}
