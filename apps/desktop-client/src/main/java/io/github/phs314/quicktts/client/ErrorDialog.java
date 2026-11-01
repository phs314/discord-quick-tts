package io.github.phs314.quicktts.client;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * 앱을 더 쓸 수 없을 때 알리는 창. 확인·Enter·Esc 로 닫는다.
 */
final class ErrorDialog {

    private ErrorDialog() {
    }

    /** 창을 띄우고 닫힐 때까지 기다린다. */
    static void show(String text) {
        Label mark = new Label("!");
        mark.getStyleClass().add("alert-mark");
        Label message = new Label(text);
        message.getStyleClass().add("alert-text");
        message.setWrapText(true);
        HBox.setHgrow(message, Priority.ALWAYS);
        HBox body = new HBox(12, mark, message);
        body.setAlignment(Pos.CENTER_LEFT);

        Button ok = ThemedWindow.button("확인", "primary");
        ThemedWindow window = new ThemedWindow("문제가 생겼어요", body, ThemedWindow.footer(null, ok));
        ok.setOnAction(event -> window.close());
        window.scene().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                window.close();
            }
        });
        window.showAndWait();
    }
}
