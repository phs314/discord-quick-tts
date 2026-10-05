package io.github.phs314.quicktts.client;

import com.github.kwhat.jnativehook.NativeHookException;
import io.github.phs314.quicktts.common.QuickChatApi;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 전역 단축키로 띄우는 quick chat 입력 위젯.
 * Enter 로 보내고 Esc 나 다른 창 클릭으로 닫는다.
 */
public class QuickChatApp extends Application {

    private static final double WIDTH = 560;

    private final TrayMenu trayMenu = new TrayMenu();
    private ClientConfig config;
    private QuickChatClient client;
    private GlobalHotkey hotkey;
    private Stage stage;
    private TextField input;

    @Override
    public void start(Stage primaryStage) {
        // 위젯이나 대화 상자를 닫아도 앱은 트레이에서 계속 돈다.
        Platform.setImplicitExit(false);

        config = ClientConfig.load();
        client = new QuickChatClient(config.serverUrl());
        if (!config.isRegistered() && !pairDevice()) {
            Platform.exit();
            return;
        }

        stage = primaryStage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        stage.setScene(createScene());
        stage.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                hide();
            }
        });

        hotkey = new GlobalHotkey(() -> Platform.runLater(this::toggle));
        try {
            hotkey.register();
        } catch (NativeHookException e) {
            showFatal("전역 단축키를 등록하지 못했습니다: " + e.getMessage());
            return;
        }

        trayMenu.install(() -> Platform.runLater(Platform::exit));
    }

    @Override
    public void stop() {
        if (hotkey != null) {
            hotkey.unregister();
        }
        trayMenu.remove();
    }

    private Scene createScene() {
        input = new TextField();
        input.setPromptText("읽어 줄 문장을 입력하고 Enter (Esc 로 닫기)");
        input.setStyle("-fx-font-size: 18px; -fx-background-radius: 8;");
        input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                send();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                hide();
            }
        });

        StackPane root = new StackPane(input);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #2b2d31; -fx-background-radius: 12;");
        return new Scene(root, WIDTH, -1);
    }

    private void toggle() {
        if (stage.isShowing()) {
            hide();
        } else {
            show();
        }
    }

    private void show() {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        stage.show();
        stage.setX(screen.getMinX() + (screen.getWidth() - stage.getWidth()) / 2);
        stage.setY(screen.getMinY() + screen.getHeight() * 0.3);
        stage.toFront();
        stage.requestFocus();
        input.requestFocus();
    }

    private void hide() {
        input.clear();
        stage.hide();
    }

    private void send() {
        String text = input.getText().strip();
        if (text.isEmpty()) {
            hide();
            return;
        }
        if (text.length() > QuickChatApi.MAX_TEXT_LENGTH) {
            trayMenu.showError("문장이 너무 깁니다. (최대 " + QuickChatApi.MAX_TEXT_LENGTH + "자)");
            return;
        }
        hide();
        client.send(config.deviceToken(), text).exceptionally(failure -> {
            Throwable cause = failure instanceof CompletionException ? failure.getCause() : failure;
            String message = cause instanceof QuickChatClient.QuickChatException
                    ? cause.getMessage()
                    : "봇 서버에 연결하지 못했습니다.";
            trayMenu.showError(message);
            if (cause instanceof QuickChatClient.DeviceUnauthorizedException) {
                Platform.runLater(this::forgetDeviceAndPairAgain);
            }
            return null;
        });
    }

    /**
     * 디스코드 {@code /연결} 로 받은 코드를 입력받아 이 PC 를 등록한다.
     *
     * @return 등록했으면 true, 사용자가 취소했으면 false
     */
    private boolean pairDevice() {
        String error = null;
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            useAppIcon(dialog);
            dialog.setTitle("Discord Quick TTS");
            dialog.setHeaderText("디스코드에서 /연결 을 입력하고 받은 코드를 붙여 넣어 주세요.");
            dialog.setContentText(error == null ? "연결 코드" : error + "\n\n연결 코드");
            Optional<String> code = dialog.showAndWait();
            if (code.isEmpty()) {
                return false;
            }
            try {
                config = config.withDeviceToken(client.register(code.get().strip()));
                config.save();
                return true;
            } catch (QuickChatClient.QuickChatException e) {
                error = e.getMessage();
            }
        }
    }

    private void forgetDeviceAndPairAgain() {
        // 여러 문장이 한꺼번에 401 을 받아도 코드 입력 창은 한 번만 띄운다.
        if (!config.isRegistered()) {
            return;
        }
        config = config.withDeviceToken("");
        config.save();
        if (!pairDevice()) {
            Platform.exit();
        }
    }

    private static void showFatal(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Discord Quick TTS");
        useAppIcon(alert);
        alert.showAndWait();
        Platform.exit();
    }

    /** 대화 상자 제목 표시줄과 작업 표시줄에 앱 아이콘을 보여 준다. */
    private static void useAppIcon(Dialog<?> dialog) {
        Stage window = (Stage) dialog.getDialogPane().getScene().getWindow();
        window.getIcons().add(new Image(QuickChatApp.class.getResource("app-icon.png").toExternalForm()));
    }
}
