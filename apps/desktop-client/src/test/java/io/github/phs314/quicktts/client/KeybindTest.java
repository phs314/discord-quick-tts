package io.github.phs314.quicktts.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.junit.jupiter.api.Test;

/**
 * 단축키를 글자로 읽고 쓰는지, 눌린 키와 맞춰 보는지 확인한다. 실제 키보드 훅은 쓰지 않는다.
 */
class KeybindTest {

    @Test
    void 기본_단축키는_Ctrl_Shift_Space() {
        assertThat(Keybind.DEFAULT).hasToString("Ctrl+Shift+Space");
    }

    @Test
    void 글자로_쓴_단축키를_읽고_같은_글자로_다시_쓴다() {
        assertThat(Keybind.parse("Ctrl+Alt+T")).get().hasToString("Ctrl+Alt+T");
        assertThat(Keybind.parse("Win+Shift+PageUp")).get().hasToString("Shift+Win+PageUp");
        assertThat(Keybind.parse(" ctrl + shift + space ")).contains(Keybind.DEFAULT);
    }

    @Test
    void 키_이름을_누르는_순서대로_나눠_준다() {
        assertThat(Keybind.parse("Win+Shift+PageUp").get().keys()).containsExactly("Shift", "Win", "PageUp");
    }

    @Test
    void F_키는_혼자_써도_된다() {
        assertThat(Keybind.parse("F9")).get().hasToString("F9");
    }

    @Test
    void 문장을_치다_눌릴_수_있는_조합은_쓰지_않는다() {
        assertThat(Keybind.parse("A")).isEmpty();
        assertThat(Keybind.parse("Shift+A")).isEmpty();
        assertThat(new Keybind(false, false, true, false, NativeKeyEvent.VC_SPACE).validationProblem()).isPresent();
    }

    @Test
    void 못_읽는_글자면_비어_있다() {
        assertThat(Keybind.parse("")).isEmpty();
        assertThat(Keybind.parse("Ctrl+Shift")).isEmpty();
        assertThat(Keybind.parse("Ctrl+A+B")).isEmpty();
        assertThat(Keybind.parse("Ctrl+없는키")).isEmpty();
    }

    @Test
    void 조합_키가_정확히_같을_때만_눌린_것으로_본다() {
        int ctrlShift = NativeKeyEvent.CTRL_L_MASK | NativeKeyEvent.SHIFT_L_MASK;
        assertThat(Keybind.DEFAULT.matches(ctrlShift, NativeKeyEvent.VC_SPACE)).isTrue();
        assertThat(Keybind.DEFAULT.matches(NativeKeyEvent.CTRL_R_MASK | NativeKeyEvent.SHIFT_R_MASK, NativeKeyEvent.VC_SPACE)).isTrue();
        assertThat(Keybind.DEFAULT.matches(ctrlShift | NativeKeyEvent.ALT_L_MASK, NativeKeyEvent.VC_SPACE)).isFalse();
        assertThat(Keybind.DEFAULT.matches(NativeKeyEvent.CTRL_L_MASK, NativeKeyEvent.VC_SPACE)).isFalse();
        assertThat(Keybind.DEFAULT.matches(ctrlShift, NativeKeyEvent.VC_ENTER)).isFalse();
    }

    @Test
    void 눌린_키를_단축키로_바꾸고_조합_키만_누르면_무시한다() {
        assertThat(Keybind.fromEvent(NativeKeyEvent.CTRL_L_MASK | NativeKeyEvent.ALT_L_MASK, NativeKeyEvent.VC_T))
                .contains(new Keybind(true, true, false, false, NativeKeyEvent.VC_T));
        assertThat(Keybind.fromEvent(NativeKeyEvent.CTRL_L_MASK, NativeKeyEvent.VC_CONTROL)).isEmpty();
        assertThat(Keybind.fromEvent(NativeKeyEvent.CTRL_L_MASK | NativeKeyEvent.SHIFT_L_MASK, NativeKeyEvent.VC_SHIFT)).isEmpty();
    }
}
