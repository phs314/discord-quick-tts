package io.github.phs314.quicktts.bot.guild.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GuildAllowlistTest {

    private static final GuildId ALLOWED = new GuildId(1L);
    private static final GuildId OTHER = new GuildId(2L);

    @Test
    void 허용_목록을_쓰면_목록에_있는_디스코드_서버에만_머문다() {
        GuildAllowlist allowlist = new GuildAllowlist(true, Set.of(ALLOWED));

        assertThat(allowlist.allows(ALLOWED)).isTrue();
        assertThat(allowlist.allows(OTHER)).isFalse();
        assertThat(allowlist.notAllowed(List.of(ALLOWED, OTHER))).containsExactly(OTHER);
    }

    @Test
    void 허용_목록을_쓰지_않으면_어디든_머문다() {
        GuildAllowlist allowlist = new GuildAllowlist(false, Set.of());

        assertThat(allowlist.allows(OTHER)).isTrue();
        assertThat(allowlist.notAllowed(List.of(ALLOWED, OTHER))).isEmpty();
        assertThat(allowlist.needsSeeding()).isFalse();
    }

    @Test
    void 허용_목록을_쓰는데_비어_있을_때만_처음_채운다() {
        assertThat(new GuildAllowlist(true, Set.of()).needsSeeding()).isTrue();
        assertThat(new GuildAllowlist(true, Set.of(ALLOWED)).needsSeeding()).isFalse();
    }
}
