package io.github.phs314.quicktts.bot.device.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import org.junit.jupiter.api.Test;

class PairingCodeTest {

    @Test
    void 만든_코드는_하이픈을_넣어_보여_주고_다시_읽을_수_있다() {
        PairingCode code = PairingCode.generate();

        assertThat(code.display()).matches("[" + PairingCode.ALPHABET + "]{4}-[" + PairingCode.ALPHABET + "]{4}");
        assertThat(new PairingCode(code.display())).isEqualTo(code);
    }

    @Test
    void 붙여_넣은_코드의_공백과_소문자를_정리한다() {
        assertThat(new PairingCode(" abcd-efgh ").value()).isEqualTo("ABCDEFGH");
    }

    @Test
    void 헷갈리는_글자나_길이가_틀린_코드는_거부한다() {
        assertThatThrownBy(() -> new PairingCode("ABCD-EFG0")).isInstanceOf(InvalidDomainValueException.class);
        assertThatThrownBy(() -> new PairingCode("ABCD")).isInstanceOf(InvalidDomainValueException.class);
    }
}
