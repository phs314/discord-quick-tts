package io.github.phs314.quicktts.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodHighlight;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

/**
 * quick chat 창을 실제로 띄워 단축키로 열기, 한글 조합, Enter 로 보내기, Esc 로 닫기를 확인한다.
 * 화면 없이 JavaFX Headless 플랫폼에서 돌고(build.gradle.kts 의 test 설정), 서버·트레이·키보드 훅은 쓰지 않는다.
 */
@ExtendWith(ApplicationExtension.class)
class QuickChatWindowTest {

    private final List<String> sent = new ArrayList<>();
    private boolean acceptSend = true;
    private int shownCount;
    private QuickChatWindow window;
    private TextField input;

    @Start
    void start(Stage ignored) {
        // 창 모양(투명, 제목 표시줄 없음)은 처음 띄우기 전에만 정할 수 있어서 테스트마다 새 창을 만든다.
        window = new QuickChatWindow(new Stage(), text -> {
            sent.add(text);
            return acceptSend;
        }, () -> shownCount++);
        input = window.inputBar().input();
    }

    @Test
    void 단축키를_누르면_입력창이_뜨고_클릭하지_않아도_바로_입력된다(FxRobot robot) {
        GlobalKeybind keybind = new GlobalKeybind(Keybind.DEFAULT, window::toggleLater);

        keybind.nativeKeyPressed(keyPressed(NativeKeyEvent.CTRL_L_MASK | NativeKeyEvent.SHIFT_L_MASK, NativeKeyEvent.VC_SPACE));
        WaitForAsyncUtils.waitForFxEvents();

        assertThat(window.isShowing()).isTrue();
        assertThat(input.isFocused()).isTrue();
        assertThat(shownCount).isEqualTo(1);
        robot.write("hello");
        assertThat(input.getText()).isEqualTo("hello");
    }

    @Test
    void 다른_키를_누르면_뜨지_않는다() {
        GlobalKeybind keybind = new GlobalKeybind(Keybind.DEFAULT, window::toggleLater);

        keybind.nativeKeyPressed(keyPressed(NativeKeyEvent.CTRL_L_MASK, NativeKeyEvent.VC_SPACE));
        WaitForAsyncUtils.waitForFxEvents();

        assertThat(window.isShowing()).isFalse();
    }

    @Test
    void 떠_있을_때_단축키를_또_누르면_닫힌다(FxRobot robot) {
        robot.interact(window::show);

        window.toggleLater();
        WaitForAsyncUtils.waitForFxEvents();

        assertThat(window.isShowing()).isFalse();
    }

    @Test
    void 닫았다_다시_띄워도_입력란에_포커스가_있다(FxRobot robot) {
        robot.interact(window::show);
        robot.interact(window::hide);
        robot.interact(window::show);

        assertThat(input.isFocused()).isTrue();
        assertThat(shownCount).isEqualTo(2);
    }

    @Test
    void 한글을_조합하는_동안_커서는_조합_글자_뒤에_있고_점선이_없다(FxRobot robot) {
        robot.interact(window::show);
        List<InputMethodEvent> seenByTextField = new ArrayList<>();
        input.addEventHandler(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, seenByTextField::add);

        // 윈도우 한글 입력기는 조합 중인 글자를 점선 표시(UNSELECTED_RAW)로, 커서 위치는 늘 0 으로 보낸다.
        robot.interact(() -> input.fireEvent(composing("ㅎ")));
        robot.interact(() -> input.fireEvent(composing("하")));

        assertThat(input.getText()).isEqualTo("하");
        assertThat(input.getCaretPosition()).isEqualTo(1);
        assertThat(seenByTextField).isNotEmpty().allSatisfy(event -> {
            assertThat(event.getCaretPosition()).isEqualTo(1);
            assertThat(event.getComposed()).extracting(InputMethodTextRun::getHighlight).containsOnlyNulls();
        });
    }

    @Test
    void 한글_조합을_마치면_글자가_입력란에_들어간다(FxRobot robot) {
        robot.interact(window::show);

        robot.interact(() -> input.fireEvent(composing("ㅇ")));
        robot.interact(() -> input.fireEvent(composing("아")));
        robot.interact(() -> input.fireEvent(composing("안")));
        robot.interact(() -> input.fireEvent(committing("안", "ㄴ")));
        robot.interact(() -> input.fireEvent(composing("녀")));
        robot.interact(() -> input.fireEvent(composing("녕")));
        robot.interact(() -> input.fireEvent(committing("녕", "")));

        assertThat(input.getText()).isEqualTo("안녕");
        assertThat(input.getCaretPosition()).isEqualTo(2);
    }

    @Test
    void Enter_를_누르면_앞뒤_공백을_뺀_문장을_보내고_닫는다(FxRobot robot) {
        robot.interact(window::show);
        robot.interact(() -> input.setText("  안녕하세요  "));

        robot.type(KeyCode.ENTER);

        assertThat(sent).containsExactly("안녕하세요");
        assertThat(window.isShowing()).isFalse();
        assertThat(input.getText()).isEmpty();
    }

    @Test
    void 빈_문장에서_Enter_를_누르면_보내지_않고_닫는다(FxRobot robot) {
        robot.interact(window::show);
        robot.interact(() -> input.setText("   "));

        robot.type(KeyCode.ENTER);

        assertThat(sent).isEmpty();
        assertThat(window.isShowing()).isFalse();
    }

    @Test
    void 보내지_못한_문장은_고칠_수_있게_창에_남긴다(FxRobot robot) {
        acceptSend = false;
        robot.interact(window::show);
        robot.interact(() -> input.setText("너무 긴 문장"));

        robot.type(KeyCode.ENTER);

        assertThat(sent).containsExactly("너무 긴 문장");
        assertThat(window.isShowing()).isTrue();
        assertThat(input.getText()).isEqualTo("너무 긴 문장");
    }

    @Test
    void Esc_를_누르면_보내지_않고_닫으며_쓰던_문장을_지운다(FxRobot robot) {
        robot.interact(window::show);
        robot.interact(() -> input.setText("안녕"));

        robot.type(KeyCode.ESCAPE);

        assertThat(sent).isEmpty();
        assertThat(window.isShowing()).isFalse();
        robot.interact(window::show);
        assertThat(input.getText()).isEmpty();
    }

    private static NativeKeyEvent keyPressed(int modifiers, int keyCode) {
        return new NativeKeyEvent(NativeKeyEvent.NATIVE_KEY_PRESSED, modifiers, 0, keyCode,
                NativeKeyEvent.CHAR_UNDEFINED, NativeKeyEvent.KEY_LOCATION_STANDARD);
    }

    /** 윈도우 한글 입력기가 조합 중에 보내는 이벤트: 점선 표시, 커서 0. */
    private InputMethodEvent composing(String composed) {
        return committing("", composed);
    }

    /** 앞 글자를 확정하면서 다음 글자 조합을 시작하는 이벤트. 조합할 글자가 없으면 확정만 한다. */
    private InputMethodEvent committing(String committed, String composed) {
        List<InputMethodTextRun> runs = composed.isEmpty()
                ? List.of()
                : List.of(new InputMethodTextRun(composed, InputMethodHighlight.UNSELECTED_RAW));
        return new InputMethodEvent(input, input, InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, runs, committed, 0);
    }
}
