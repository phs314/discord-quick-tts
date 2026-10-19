package io.github.phs314.quicktts.bot.guild.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.DisallowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeAllowedGuildPort;
import io.github.phs314.quicktts.bot.guild.application.port.out.FakeGuildAllowlistEventPort;
import io.github.phs314.quicktts.bot.guild.domain.AllowedGuild;
import io.github.phs314.quicktts.bot.guild.domain.event.GuildDisallowed;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DisallowGuildServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");
    private static final DiscordUserId OWNER = new DiscordUserId(100L);
    private static final DiscordUserId SOMEONE = new DiscordUserId(200L);
    private static final GuildId MINE = new GuildId(1L);
    private static final GuildId FRIEND = new GuildId(2L);

    private final FakeAllowedGuildPort allowedGuilds = new FakeAllowedGuildPort();
    private final FakeGuildAllowlistEventPort events = new FakeGuildAllowlistEventPort();
    private final DisallowGuildService service = new DisallowGuildService(() -> OWNER, allowedGuilds, events);

    /** 봇이 그 서버에서 나가는 일은 커밋 뒤에 이벤트를 받는 GuildDisallowedListener 가 한다. */
    @Test
    void 목록에서_빼면_목록에서_빠지고_뺐다는_이벤트를_한_번_낸다() {
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(FRIEND, NOW));

        assertThat(service.disallow(new DisallowGuildCommand(OWNER, MINE))).isTrue();
        assertThat(service.disallow(new DisallowGuildCommand(OWNER, MINE))).isFalse();

        assertThat(allowedGuilds.findAll()).extracting(AllowedGuild::guildId).containsExactly(FRIEND);
        assertThat(events.published()).containsExactly(new GuildDisallowed(MINE));
    }

    @Test
    void 봇_주인이_아니면_뺄_수_없다() {
        allowedGuilds.saveIfAbsent(AllowedGuild.allow(MINE, NOW));

        assertThatThrownBy(() -> service.disallow(new DisallowGuildCommand(SOMEONE, MINE)))
                .isInstanceOf(NotBotOwnerException.class);
        assertThat(allowedGuilds.findAll()).extracting(AllowedGuild::guildId).containsExactly(MINE);
        assertThat(events.published()).isEmpty();
    }
}
