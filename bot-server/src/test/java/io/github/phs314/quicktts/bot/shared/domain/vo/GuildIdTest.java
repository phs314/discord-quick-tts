package io.github.phs314.quicktts.bot.shared.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class GuildIdTest {

    @Test
    void 디스코드에서_복사한_ID_를_읽는다() {
        assertThat(GuildId.parse(" 987654321098765432 ")).isEqualTo(new GuildId(987654321098765432L));
    }

    @Test
    void 숫자가_아니면_읽지_않는다() {
        assertThatThrownBy(() -> GuildId.parse("내 서버")).isInstanceOf(InvalidDomainValueException.class);
        assertThatThrownBy(() -> GuildId.parse("-1")).isInstanceOf(InvalidDomainValueException.class);
    }
}
