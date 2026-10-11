package io.github.phs314.quicktts.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

/**
 * 트레이 메뉴의 Change Shortcut... 으로 여는 창. 새 단축키를 직접 눌러서 정한다.
 * 키 입력은 {@link GlobalHotkey} 가 받아 {@link #pressed(Hotkey)} 로 넘겨 준다.
 */
final class HotkeyDialog extends Dialog<Hotkey> {

    private static final ButtonType SAVE = new ButtonType("저장", ButtonBar.ButtonData.OK_DONE);
    private static final ButtonType CANCEL = new ButtonType("취소", ButtonBar.ButtonData.CANCEL_CLOSE);
    private static final ButtonType RESET = new ButtonType("기본값 (" + Hotkey.DEFAULT + ")", ButtonBar.ButtonData.LEFT);

    private final Label combo = new Label();
    private final Label message = new Label();
    private final Button saveButton;
    private Hotkey chosen;

    HotkeyDialog(Hotkey current) {
        setTitle("Discord Quick TTS");
        setHeaderText("입력창을 열 새 단축키를 눌러 주세요.");

        combo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        message.setWrapText(true);
        message.setMaxWidth(360);
        // 안내가 두 줄이 되어도 창 크기가 그대로이도록 처음부터 두 줄 자리를 잡아 둔다.
        message.setMinHeight(40);
        message.setAlignment(Pos.TOP_LEFT);
        VBox content = new VBox(10, combo, message);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(10, 10, 0, 10));
        content.setMinWidth(380);
        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().addAll(RESET, CANCEL, SAVE);

        saveButton = (Button) getDialogPane().lookupButton(SAVE);
        // Enter 가 들어간 단축키를 누르다가 저장되지 않도록 기본 버튼으로 두지 않는다.
        saveButton.setDefaultButton(false);
        Button resetButton = (Button) getDialogPane().lookupButton(RESET);
        resetButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            pressed(Hotkey.DEFAULT);
        });

        // 누른 키는 GlobalHotkey 로 받으니, 창 안에서는 Space 로 버튼이 눌리는 일 없게 키 입력을 막는다. Esc 만 닫기로 둔다.
        getDialogPane().getScene().addEventFilter(KeyEvent.ANY, event -> {
            boolean plainEscape = event.getCode() == KeyCode.ESCAPE && !event.isControlDown()
                    && !event.isAltDown() && !event.isShiftDown() && !event.isMetaDown();
            if (!plainEscape) {
                event.consume();
            }
        });

        setResultConverter(button -> button == SAVE ? chosen : null);
        display(current, "지금 쓰는 단축키예요. 바꾸려면 새 조합을 누르세요.", false);
    }

    /** 누른 키 조합을 보여 준다. 어느 스레드에서 불러도 된다. */
    void pressed(Hotkey hotkey) {
        Platform.runLater(() -> {
            if (hotkey.problem().isPresent()) {
                display(hotkey, hotkey.problem().get(), false);
            } else {
                chosen = hotkey;
                display(hotkey, hotkey.win()
                        ? "저장을 누르면 바로 바뀌어요. Win 이 들어간 조합은 윈도우가 먼저 가로챌 수 있어요."
                        : "저장을 누르면 바로 바뀌어요.", true);
            }
        });
    }

    private void display(Hotkey hotkey, String text, boolean canSave) {
        combo.setText(hotkey.toString());
        message.setText(text);
        saveButton.setDisable(!canSave);
    }
}
