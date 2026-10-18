package io.github.phs314.quicktts.client;

import java.util.Optional;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * 트레이 메뉴의 Change Shortcut... 으로 여는 창. 새 단축키를 직접 눌러서 정한다.
 * 키 입력은 {@link GlobalHotkey} 가 받아 {@link #onPressed(Hotkey)} 로 넘겨 준다.
 */
final class HotkeyDialog {

    private static final PseudoClass NEW = PseudoClass.getPseudoClass("new");
    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private static final PseudoClass WARNING = PseudoClass.getPseudoClass("warning");
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private final Label tag = new Label();
    private final HBox keys = new HBox(6);
    private final VBox keyBox = new VBox(10, tag, keys);
    private final Label message = new Label();
    private final Button saveButton = ThemedWindow.button("저장", "primary");
    private final ThemedWindow window;
    private Hotkey chosen;
    private Hotkey result;

    HotkeyDialog(Hotkey current) {
        window = new ThemedWindow("단축키 바꾸기", body(), footer());
        // 누른 키는 GlobalHotkey 로 받으니, 창 안에서는 Space 로 버튼이 눌리는 일 없게 키 입력을 막는다. Esc 만 닫기로 둔다.
        window.scene().addEventFilter(KeyEvent.ANY, event -> {
            boolean plainEscape = event.getCode() == KeyCode.ESCAPE && !event.isControlDown()
                    && !event.isAltDown() && !event.isShiftDown() && !event.isMetaDown();
            if (!plainEscape) {
                event.consume();
            }
        });
        display(current, "지금 쓰는 단축키예요. 바꾸려면 새 조합을 누르세요.", false);
    }

    /** 창을 띄우고 닫힐 때까지 기다린다. 저장을 눌렀을 때만 새 단축키가 있다. */
    Optional<Hotkey> showAndWait() {
        window.showAndWait();
        return Optional.ofNullable(result);
    }

    /** 누른 키 조합을 보여 준다. 어느 스레드에서 불러도 된다. */
    void onPressed(Hotkey hotkey) {
        Platform.runLater(() -> {
            if (hotkey.validationProblem().isPresent()) {
                chosen = null;
                display(hotkey, hotkey.validationProblem().get(), false);
            } else {
                chosen = hotkey;
                display(hotkey, hotkey.win()
                        ? "저장을 누르면 바로 바뀌어요. Win 이 들어간 조합은 윈도우가 먼저 가로챌 수 있어요."
                        : "저장을 누르면 바로 바뀌어요.", true);
            }
        });
    }

    private Node body() {
        Label prompt = new Label("입력창을 열 새 단축키를 눌러 주세요.");
        prompt.getStyleClass().add("prompt");
        tag.getStyleClass().add("key-tag");
        keys.setAlignment(Pos.CENTER_LEFT);
        keyBox.getStyleClass().add("key-box");
        message.getStyleClass().add("message");
        message.setWrapText(true);
        // 안내가 두 줄이 되어도 창 크기가 그대로이도록 처음부터 두 줄 자리를 잡아 둔다.
        message.setMinHeight(34);
        message.setAlignment(Pos.TOP_LEFT);
        return new VBox(10, prompt, keyBox, message);
    }

    private Node footer() {
        Button reset = ThemedWindow.button("기본값으로 (" + Hotkey.DEFAULT + ")", "reset");
        reset.setOnAction(event -> onPressed(Hotkey.DEFAULT));
        Button cancel = ThemedWindow.button("취소", "cancel");
        cancel.setOnAction(event -> window.close());
        saveButton.setOnAction(event -> {
            result = chosen;
            window.close();
        });
        return ThemedWindow.footer(reset, cancel, saveButton);
    }

    private void display(Hotkey hotkey, String text, boolean canSave) {
        boolean invalid = hotkey.validationProblem().isPresent();
        tag.setText(canSave || invalid ? "새 단축키" : "지금 단축키");
        keys.getChildren().clear();
        for (String name : hotkey.keys()) {
            if (!keys.getChildren().isEmpty()) {
                keys.getChildren().add(styled(new Label("+"), "key-plus"));
            }
            keys.getChildren().add(styled(new Label(name), "keycap"));
        }
        keyBox.pseudoClassStateChanged(NEW, canSave);
        keyBox.pseudoClassStateChanged(INVALID, invalid);
        message.setText(text);
        message.pseudoClassStateChanged(ERROR, invalid);
        message.pseudoClassStateChanged(WARNING, canSave && hotkey.win());
        saveButton.setDisable(!canSave);
    }

    private static Label styled(Label label, String styleClass) {
        label.getStyleClass().add(styleClass);
        return label;
    }
}
