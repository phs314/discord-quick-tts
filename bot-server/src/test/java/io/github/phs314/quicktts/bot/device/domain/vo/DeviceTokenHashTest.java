package io.github.phs314.quicktts.bot.device.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class DeviceTokenHashTest {

    @Test
    void 같은_토큰은_늘_같은_해시가_되고_원문과는_다르다() {
        DeviceToken token = DeviceToken.generate();

        assertThat(token.hash()).isEqualTo(new DeviceToken(token.value()).hash());
        assertThat(token.hash().value()).hasSize(DeviceTokenHash.LENGTH).isNotEqualTo(token.value());
    }

    @Test
    void 토큰_원문은_해시로_만들_수_없다() {
        String raw = DeviceToken.generate().value();

        assertThatThrownBy(() -> new DeviceTokenHash(raw)).isInstanceOf(InvalidDomainValueException.class);
    }
}
