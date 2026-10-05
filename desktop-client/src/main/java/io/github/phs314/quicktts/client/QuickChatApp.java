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
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 전역 단축키로 화면 아래쪽 가운데에 띄우는 quick chat 입력 위젯.
 * Enter 로 보내고 Esc 나 다른 창 클릭으로 닫는다.
 */
public class QuickChatApp extends Application {

    /** 막대 둘레의 그림자 여백. */
    private static final double SHADOW_MARGIN = 24;
    /** 화면 높이에서 막대 아래쪽 끝까지의 비율. 0.15 면 화면 맨 아래에서 15% 위에 막대가 놓인다. */
    private static final double BOTTOM_RATIO = 0.15;

    /** {@link Launcher} 가 먼저 잡아 둔 중복 실행 방지 자리. 클래스패스로 바로 띄우면 null 이다. */
    static SingleInstance singleInstance;

    private final TrayMenu trayMenu = new TrayMenu();
    private ClientConfig config;
    private QuickChatClient client;
    private LocalBotServer localBotServer;
    private GlobalHotkey hotkey;
    private Stage stage;
    private InputBar inputBar;
    private TextField input;

    @Override
    public void start(Stage primaryStage) {
        // 위젯이나 대화 상자를 닫아도 앱은 트레이에서 계속 돈다.
        Platform.setImplicitExit(false);

        config = ClientConfig.load();
        // exe 로 실행했고 서버가 이 PC 면, 봇 서버도 같이 켠다.
        localBotServer = LocalBotServer.startIfNeeded(config.serverUrl());
        client = new QuickChatClient(config.serverUrl());
        if (!config.isRegistered() && !pairDevice()) {
            Platform.exit();
            return;
        }

        stage = primaryStage;
        stage.initStyle(StageStyle.TRANSPARENT);
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

        trayMenu.install(() -> Platform.runLater(Platform::exit), WindowsStartup.forThisApp());
        // 이미 떠 있는데 exe 를 또 실행하면 새로 뜨는 대신 입력창을 연다.
        if (singleInstance != null) {
            singleInstance.onShowRequested(() -> Platform.runLater(this::show));
        }
    }

    @Override
    public void stop() {
        if (localBotServer != null) {
            localBotServer.close();
        }
        if (singleInstance != null) {
            singleInstance.close();
        }
        if (hotkey != null) {
            hotkey.unregister();
        }
        trayMenu.remove();
    }

    private Scene createScene() {
        inputBar = new InputBar();
        input = inputBar.input();
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

    private void toggle() {
        if (stage.isShowing()) {
            hide();
        } else {
            show();
        }
    }

    /** 화면 아래쪽 가운데(맨 아래에서 {@link #BOTTOM_RATIO} 위)에 띄우고 문장이 읽힐 음성 채널을 다시 확인한다. */
    private void show() {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        Rectangle2D usable = Screen.getPrimary().getVisualBounds();
        stage.show();
        stage.setX(screen.getMinX() + (screen.getWidth() - stage.getWidth()) / 2);
        double barBottom = screen.getMaxY() - screen.getHeight() * BOTTOM_RATIO;
        // 작업 표시줄이 아주 커도 막대가 그 뒤에 숨지 않게 한다.
        barBottom = Math.min(barBottom, usable.getMaxY());
        stage.setY(barBottom - stage.getHeight() + SHADOW_MARGIN);
        stage.toFront();
        stage.requestFocus();
        input.requestFocus();
        refreshVoiceChannel();
    }

    private void refreshVoiceChannel() {
        inputBar.showChecking();
        client.myVoiceChannel(config.deviceToken()).whenComplete((channel, failure) -> Platform.runLater(() -> {
            if (failure == null) {
                inputBar.showChannel(channel);
            } else {
                inputBar.showServerUnreachable();
            }
        }));
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
