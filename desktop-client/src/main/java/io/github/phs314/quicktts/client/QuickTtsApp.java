package io.github.phs314.quicktts.client;

import com.github.kwhat.jnativehook.NativeHookException;
import io.github.phs314.quicktts.common.QuickTtsApi;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import javafx.application.Application;
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
 * QuickTTS 트레이 앱. 전역 단축키로 화면 아래쪽 가운데에 quick chat 입력 위젯을 띄운다.
 * Enter 로 보내고 Esc 나 다른 창 클릭으로 닫는다.
 */
public class QuickTtsApp extends Application {

    /** 막대 둘레의 그림자 여백. */
    private static final double SHADOW_MARGIN = 24;
    /** 화면 높이에서 막대 아래쪽 끝까지의 비율. 0.15 면 화면 맨 아래에서 15% 위에 막대가 놓인다. */
    private static final double BOTTOM_RATIO = 0.15;
    /** 입력창 창 제목. 화면에는 안 보이고, 윈도우에서 창을 찾아 앞으로 가져올 때 쓴다. */
    private static final String WINDOW_TITLE = "QuickTTS Quick Chat";

    /** {@link Launcher} 가 먼저 잡아 둔 중복 실행 방지 자리. 클래스패스로 바로 띄우면 null 이다. */
    static SingleInstance singleInstance;

    private final TrayMenu trayMenu = new TrayMenu();
    private ClientConfig config;
    private QuickTtsClient client;
    private LocalBotServer localBotServer;
    private GlobalHotkey hotkey;
    private Stage stage;
    private InputBar inputBar;
    private TextField input;
    /** 단축키 바꾸기 창이 이미 떠 있으면 트레이 메뉴를 또 눌러도 하나만 띄운다. */
    private boolean changingHotkey;

    @Override
    public void start(Stage primaryStage) {
        // 위젯이나 대화 상자를 닫아도 앱은 트레이에서 계속 돈다.
        Platform.setImplicitExit(false);

        config = ClientConfig.load();
        // exe 로 실행했고 서버가 이 PC 면, 봇 서버도 같이 켠다.
        localBotServer = LocalBotServer.startIfNeeded(config.serverUrl());
        client = new QuickTtsClient(config.serverUrl());
        if (!config.isRegistered() && !pairDevice()) {
            Platform.exit();
            return;
        }

        stage = primaryStage;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle(WINDOW_TITLE);
        stage.setAlwaysOnTop(true);
        stage.setScene(createScene());
        stage.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                hide();
            }
        });

        hotkey = new GlobalHotkey(config.hotkey(), () -> Platform.runLater(this::toggle));
        try {
            hotkey.register();
        } catch (NativeHookException e) {
            showFatal("전역 단축키를 등록하지 못했습니다: " + e.getMessage());
            return;
        }

        trayMenu.install(config.hotkey(), () -> Platform.runLater(this::changeHotkey),
                () -> Platform.runLater(Platform::exit), WindowsStartup.forThisApp());
        // 이미 떠 있는데 exe 를 또 실행하면 새로 뜨는 대신 입력창을 연다.
        if (singleInstance != null) {
            singleInstance.onShowRequested(() -> Platform.runLater(this::show));
        }
        // 새 버전이 있으면 트레이 알림으로만 알린다. 확인하지 못해도 조용히 넘어간다.
        UpdateChecker.forThisApp().ifPresent(checker ->
                checker.newerRelease().thenAccept(release -> release.ifPresent(trayMenu::showUpdate)));
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
        refreshVoiceChannel();
    }

    private void refreshVoiceChannel() {
        inputBar.showChecking();
        client.findMyVoiceChannel(config.deviceToken()).whenComplete((channel, failure) -> Platform.runLater(() -> {
            Throwable cause = failure instanceof CompletionException ? failure.getCause() : failure;
            if (failure == null) {
                inputBar.showChannel(channel);
            } else if (cause instanceof QuickTtsClient.DeviceUnauthorizedException) {
                hide();
                trayMenu.showError(cause.getMessage());
                forgetDeviceAndPairAgain();
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
        if (text.length() > QuickTtsApi.MAX_TEXT_LENGTH) {
            trayMenu.showError("문장이 너무 깁니다. (최대 " + QuickTtsApi.MAX_TEXT_LENGTH + "자)");
            return;
        }
        hide();
        client.send(config.deviceToken(), text).exceptionally(failure -> {
            Throwable cause = failure instanceof CompletionException ? failure.getCause() : failure;
            String message = cause instanceof QuickTtsClient.QuickTtsClientException
                    ? cause.getMessage()
                    : "봇 서버에 연결하지 못했습니다.";
            trayMenu.showError(message);
            if (cause instanceof QuickTtsClient.DeviceUnauthorizedException) {
                Platform.runLater(this::forgetDeviceAndPairAgain);
            }
            return null;
        });
    }

    /** 새 단축키를 눌러서 정하는 창을 띄우고, 저장하면 설정 파일에 쓰고 바로 바꾼다. */
    private void changeHotkey() {
        if (changingHotkey) {
            return;
        }
        changingHotkey = true;
        HotkeyDialog dialog = new HotkeyDialog(config.hotkey());
        hotkey.startRecording(dialog::onPressed);
        try {
            dialog.showAndWait().ifPresent(newHotkey -> {
                config = config.withHotkey(newHotkey);
                config.save();
                hotkey.change(newHotkey);
                trayMenu.showHotkey(newHotkey);
            });
        } finally {
            hotkey.stopRecording();
            changingHotkey = false;
        }
    }

    /**
     * 디스코드 {@code /연결} 로 받은 코드를 입력받아 이 PC 를 등록한다.
     *
     * @return 등록했으면 true, 사용자가 취소했으면 false
     */
    private boolean pairDevice() {
        String error = null;
        while (true) {
            Optional<String> code = new PairingDialog(error).showAndWait();
            if (code.isEmpty()) {
                return false;
            }
            try {
                config = config.withDeviceToken(client.registerDevice(code.get()));
                config.save();
                return true;
            } catch (QuickTtsClient.QuickTtsClientException e) {
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
        ErrorDialog.show(message);
        Platform.exit();
    }
}
