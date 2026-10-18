package io.github.phs314.quicktts.client;

import java.util.Optional;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * 디스코드 {@code /연결} 로 받은 코드를 붙여 넣는 창. 앞서 코드가 틀렸으면 그 이유를 빨간 글자로 같이 보여 준다.
 */
final class PairingDialog {

    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private final TextField code = new TextField();
    private final Button connectButton = ThemedWindow.button("연결", "primary");
    private final ThemedWindow window;
    private String result;

    /** @param error 앞서 넣은 코드가 안 된 이유. 처음이면 null */
    PairingDialog(String error) {
        window = new ThemedWindow("이 PC 연결하기", body(error), footer());
    }

    /** 창을 띄우고 닫힐 때까지 기다린다. 연결을 눌렀을 때만 코드가 있다. */
    Optional<String> showAndWait() {
        Platform.runLater(code::requestFocus);
        window.showAndWait();
        return Optional.ofNullable(result);
    }

    private Node body(String error) {
        Label prompt = new Label("디스코드에서 /연결 을 입력하고 받은 코드를 붙여 넣어 주세요.");
        prompt.getStyleClass().add("prompt");
        prompt.setWrapText(true);
        code.setPromptText("8자리 연결 코드");
        code.pseudoClassStateChanged(INVALID, error != null);
        code.textProperty().addListener((observable, before, text) -> {
            connectButton.setDisable(text.isBlank());
            // 코드를 다시 넣기 시작하면 빨간 테두리는 거둔다. 이유 글은 남겨 둔다.
            code.pseudoClassStateChanged(INVALID, false);
        });
        code.setOnAction(event -> connect());
        connectButton.setDisable(true);

        VBox body = new VBox(10, prompt, code);
        if (error != null) {
            Label message = new Label(error);
            message.getStyleClass().add("message");
            message.pseudoClassStateChanged(ERROR, true);
            message.setWrapText(true);
            body.getChildren().add(message);
        }
        return body;
    }

    private Node footer() {
        Button cancel = ThemedWindow.button("취소", "cancel");
        cancel.setOnAction(event -> window.close());
        connectButton.setOnAction(event -> connect());
        return ThemedWindow.footer(null, cancel, connectButton);
    }

    private void connect() {
        if (code.getText().isBlank()) {
            return;
        }
        result = code.getText().strip();
        window.close();
    }
}
