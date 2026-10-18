package io.github.phs314.quicktts.bot.guild.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.AllowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.DisallowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.AllowedGuildDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.query.GetAllowlistQuery;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeAllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeGuildAllowlistEventPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeGuildAllowlistSettingPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.guild.domain.event.GuildDisallowed;
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

    private final FakeAllowedGuildPort allowedGuilds = new FakeAllowedGuildPort();
    private final FakeGuildAllowlistSettingPort setting = new FakeGuildAllowlistSettingPort();
    private final FakeGuildAllowlistEventPort events = new FakeGuildAllowlistEventPort();
    private final ManageGuildAllowlistService service = new ManageGuildAllowlistService(
            () -> OWNER, allowedGuilds, setting, events, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void 봇_주인은_서버를_넣고_볼_수_있다() {
        assertThat(service.allow(new AllowGuildCommand(OWNER, FRIEND))).isTrue();
        assertThat(service.allow(new AllowGuildCommand(OWNER, FRIEND))).isFalse();

        var allowlist = service.getAllowlist(new GetAllowlistQuery(OWNER));
        assertThat(allowlist.enabled()).isTrue();
        assertThat(allowlist.guilds()).containsExactly(new AllowedGuildDto(FRIEND, NOW));
    }

    @Test
    void 봇_주인이_아니면_아무것도_할_수_없다() {
        assertThatThrownBy(() -> service.allow(new AllowGuildCommand(SOMEONE, FRIEND)))
                .isInstanceOf(NotBotOwnerException.class);
        assertThatThrownBy(() -> service.disallow(new DisallowGuildCommand(SOMEONE, MINE)))
                .isInstanceOf(NotBotOwnerException.class);
        assertThatThrownBy(() -> service.getAllowlist(new GetAllowlistQuery(SOMEONE)))
                .isInstanceOf(NotBotOwnerException.class);
        assertThat(allowedGuilds.findAll()).isEmpty();
        assertThat(events.published()).isEmpty();
    }

    /** 봇이 그 서버에서 나가는 일은 커밋 뒤에 이벤트를 받는 GuildDisallowedListener 가 한다. */
    @Test
    void 목록에서_빼면_목록에서_빠지고_뺐다는_이벤트를_한_번_낸다() {
        service.allow(new AllowGuildCommand(OWNER, MINE));
        service.allow(new AllowGuildCommand(OWNER, FRIEND));

        assertThat(service.disallow(new DisallowGuildCommand(OWNER, MINE))).isTrue();
        assertThat(service.disallow(new DisallowGuildCommand(OWNER, MINE))).isFalse();

        assertThat(allowedGuilds.findAll()).extracting(AllowedGuild::guildId).containsExactly(FRIEND);
        assertThat(events.published()).containsExactly(new GuildDisallowed(MINE));
    }

    @Test
    void 허용_목록이_꺼져_있어도_넣고_뺄_수_있다() {
        setting.setEnabled(false);
        service.allow(new AllowGuildCommand(OWNER, MINE));

        assertThat(service.disallow(new DisallowGuildCommand(OWNER, MINE))).isTrue();

        assertThat(service.getAllowlist(new GetAllowlistQuery(OWNER)).enabled()).isFalse();
    }
}
