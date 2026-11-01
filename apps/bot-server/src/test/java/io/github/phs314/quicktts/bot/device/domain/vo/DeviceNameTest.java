package io.github.phs314.quicktts.bot.device.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DeviceNameTest {

    @Test
    void 이름이_없으면_기본_이름을_쓴다() {
        assertThat(new DeviceName(null).value()).isEqualTo("이름 없는 PC");
        assertThat(new DeviceName("  ").value()).isEqualTo("이름 없는 PC");
    }

    @Test
    void 앞뒤_공백을_지우고_너무_길면_자른다() {
        assertThat(new DeviceName("  현수-PC ").value()).isEqualTo("현수-PC");
        assertThat(new DeviceName("가".repeat(80)).value()).hasSize(DeviceName.MAX_LENGTH);
    }
}
