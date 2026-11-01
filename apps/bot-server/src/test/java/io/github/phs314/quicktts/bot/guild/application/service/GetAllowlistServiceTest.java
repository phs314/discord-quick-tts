package io.github.phs314.quicktts.bot.guild.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.AllowedGuildDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.query.GetAllowlistQuery;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeAllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeGuildAllowlistConfigPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class GetAllowlistServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");
    private static final DiscordUserId OWNER = new DiscordUserId(100L);
    private static final DiscordUserId SOMEONE = new DiscordUserId(200L);
    private static final GuildId MINE = new GuildId(1L);
    private static final GuildId FRIEND = new GuildId(2L);

    private final FakeAllowedGuildPort allowedGuilds = new FakeAllowedGuildPort();
    private final FakeGuildAllowlistConfigPort setting = new FakeGuildAllowlistConfigPort();
    private final GetAllowlistService service = new GetAllowlistService(() -> OWNER, allowedGuilds, setting);

    @Test
    void 봇_주인은_허용_목록을_볼_수_있다() {
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(FRIEND, NOW));

        var allowlist = service.getAllowlist(new GetAllowlistQuery(OWNER));
        assertThat(allowlist.enabled()).isTrue();
        assertThat(allowlist.guilds()).containsExactly(new AllowedGuildDto(FRIEND, NOW));
    }

    @Test
    void 허용_목록이_꺼져_있으면_꺼졌다고_보인다() {
        setting.setEnabled(false);
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));

        var allowlist = service.getAllowlist(new GetAllowlistQuery(OWNER));
        assertThat(allowlist.enabled()).isFalse();
        assertThat(allowlist.guilds()).containsExactly(new AllowedGuildDto(MINE, NOW));
    }

    @Test
    void 봇_주인이_아니면_볼_수_없다() {
        assertThatThrownBy(() -> service.getAllowlist(new GetAllowlistQuery(SOMEONE)))
                .isInstanceOf(NotBotOwnerException.class);
    }
}
