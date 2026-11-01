package io.github.phs314.quicktts.client;

import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 단축키로 화면 아래쪽 가운데에 띄우는 quick chat 창.
 * Enter 로 보내고 Esc 나 다른 창 클릭으로 닫는다. 보낸 문장을 서버로 넘기는 일은 {@link QuickTtsApp} 이 한다.
 */
final class QuickChatWindow {

    /** 막대 둘레의 그림자 여백. */
    private static final double SHADOW_MARGIN = 24;
    /** 화면 높이에서 막대 아래쪽 끝까지의 비율. 0.15 면 화면 맨 아래에서 15% 위에 막대가 놓인다. */
    static final double BOTTOM_RATIO = 0.15;
    /** 입력창 창 제목. 화면에는 안 보이고, 윈도우에서 창을 찾아 앞으로 가져올 때 쓴다. */
    static final String WINDOW_TITLE = "QuickTTS Quick Chat";

    private final Stage stage;
    private final InputBar inputBar = new InputBar();
    private final TextField input = inputBar.input();
    private final Predicate<String> onSend;
    private final Runnable onShown;

    /**
     * @param onSend Enter 로 보낸 문장(앞뒤 공백을 뺀, 비어 있지 않은 문장)을 받는다. 받았으면 true 를 돌려주고 창이 닫힌다.
     *               false 면 문장을 고칠 수 있게 창을 그대로 둔다.
     * @param onShown 창을 띄운 뒤에 불린다.
     */
    QuickChatWindow(Stage stage, Predicate<String> onSend, Runnable onShown) {
        this.stage = stage;
        this.onSend = onSend;
        this.onShown = onShown;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle(WINDOW_TITLE);
        stage.setAlwaysOnTop(true);
        stage.setScene(createScene());
        stage.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                hide();
            }
        });
    }

    InputBar inputBar() {
        return inputBar;
    }

    boolean isShowing() {
        return stage.isShowing();
    }

    /** 전역 단축키 스레드처럼 FX 스레드가 아닌 곳에서 불러도 된다. */
    void toggleLater() {
        Platform.runLater(this::toggle);
    }

    void toggle() {
        if (stage.isShowing()) {
            hide();
        } else {
            show();
        }
    }

    /** 화면 아래쪽 가운데(맨 아래에서 {@link #BOTTOM_RATIO} 위)에 띄운다. */
    void show() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        Rectangle2D usable = Screen.getPrimary().getVisualBounds();
        WindowsForeground.allowActivation();
        stage.show();
        stage.setX(screen.getMinX() + (screen.getWidth() - stage.getWidth()) / 2);
        double barBottom = screen.getMaxY() - screen.getHeight() * BOTTOM_RATIO;
        // 작업 표시줄이 아주 커도 막대가 그 뒤에 숨지 않게 한다.
        barBottom = Math.min(barBottom, usable.getMaxY());
        stage.setY(barBottom - stage.getHeight() + SHADOW_MARGIN);
        stage.toFront();
        stage.requestFocus();
        WindowsForeground.activate(WINDOW_TITLE);
        // 창을 숨겼다 다시 띄우면 JavaFX 가 새 창을 만들면서 한글 조합 입력(IME) 연결을 잃는다.
        // 포커스가 이미 입력란에 있으면 다시 연결하지 않으므로, 막대로 옮겼다가 입력란으로 돌려서 다시 연결한다.
        // 연결이 없으면 조합 중인 글자가 화면 왼쪽 위의 따로 뜬 창에 보이고 입력란에는 한 글자씩 늦게 들어온다.
        inputBar.node().requestFocus();
        input.requestFocus();
        onShown.run();
    }

    void hide() {
        input.clear();
        stage.hide();
    }

    private Scene createScene() {
        input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                send();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                hide();
            }
        });

        // 그림자가 잘리지 않도록 막대 둘레에 투명한 여백을 둔다.
        StackPane root = new StackPane(inputBar.node());
        root.setPadding(new Insets(SHADOW_MARGIN));
        root.setStyle("-fx-background-color: transparent;");
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        return scene;
    }

    private void send() {
        String text = input.getText().strip();
        if (text.isEmpty() || onSend.test(text)) {
            hide();
        }
    }
}
