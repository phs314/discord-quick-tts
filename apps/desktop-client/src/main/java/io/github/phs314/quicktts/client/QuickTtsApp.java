package io.github.phs314.quicktts.client;

import com.github.kwhat.jnativehook.NativeHookException;
import io.github.phs314.quicktts.api.QuickTtsApi;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * QuickTTS 트레이 앱. 전역 단축키로 quick chat 창({@link QuickChatWindow})을 띄우고,
 * 거기서 보낸 문장을 봇 서버로 보낸다.
 */
public class QuickTtsApp extends Application {

    /** {@link Launcher} 가 먼저 잡아 둔 중복 실행 방지 자리. 클래스패스로 바로 띄우면 null 이다. */
    static SingleInstance singleInstance;

    private final TrayMenu trayMenu = new TrayMenu();
    private ClientConfig config;
    private QuickTtsClient client;
    private LocalBotServer localBotServer;
    private GlobalKeybind keybind;
    private QuickChatWindow window;
    /** 단축키 바꾸기 창이 이미 떠 있으면 트레이 메뉴를 또 눌러도 하나만 띄운다. */
    private boolean changingKeybind;

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

        window = new QuickChatWindow(primaryStage, this::send, this::refreshVoiceChannel);

        keybind = new GlobalKeybind(config.keybind(), window::toggleLater);
        try {
            keybind.register();
        } catch (NativeHookException e) {
            showFatal("전역 단축키를 등록하지 못했습니다: " + e.getMessage());
            return;
        }

        trayMenu.install(config.keybind(), () -> Platform.runLater(this::changeKeybind),
                () -> Platform.runLater(Platform::exit), WindowsStartup.forThisApp());
        // 이미 떠 있는데 exe 를 또 실행하면 새로 뜨는 대신 입력창을 연다.
        if (singleInstance != null) {
            singleInstance.onShowRequested(() -> Platform.runLater(window::show));
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
        if (keybind != null) {
            keybind.unregister();
        }
        trayMenu.remove();
    }

    /** 창을 띄울 때마다 문장이 읽힐 음성 채널을 다시 확인한다. */
    private void refreshVoiceChannel() {
        InputBar inputBar = window.inputBar();
        inputBar.showChecking();
        client.findMyVoiceChannel(config.deviceToken()).whenComplete((channel, failure) -> Platform.runLater(() -> {
            Throwable cause = failure instanceof CompletionException ? failure.getCause() : failure;
            if (failure == null) {
                inputBar.showChannel(channel);
            } else if (cause instanceof QuickTtsClient.InvalidDeviceTokenException) {
                window.hide();
                trayMenu.showError(cause.getMessage());
                forgetDeviceAndPairAgain();
            } else {
                inputBar.showServerUnreachable();
            }
        }));
    }

    /**
     * 창에서 Enter 로 보낸 문장을 봇 서버로 보낸다. 실패는 트레이 알림으로 알린다.
     *
     * @return 보냈으면 true, 너무 길어서 창을 그대로 둬야 하면 false
     */
    private boolean send(String text) {
        if (text.length() > QuickTtsApi.MAX_CONTENT_LENGTH) {
            trayMenu.showError("문장이 너무 깁니다. (최대 " + QuickTtsApi.MAX_CONTENT_LENGTH + "자)");
            return false;
        }
        client.send(config.deviceToken(), text).exceptionally(failure -> {
            Throwable cause = failure instanceof CompletionException ? failure.getCause() : failure;
            String message = cause instanceof QuickTtsClient.QuickTtsClientException
                    ? cause.getMessage()
                    : "봇 서버에 연결하지 못했습니다.";
            trayMenu.showError(message);
            if (cause instanceof QuickTtsClient.InvalidDeviceTokenException) {
                Platform.runLater(this::forgetDeviceAndPairAgain);
            }
            return null;
        });
        return true;
    }

    /** 새 단축키를 눌러서 정하는 창을 띄우고, 저장하면 설정 파일에 쓰고 바로 바꾼다. */
    private void changeKeybind() {
        if (changingKeybind) {
            return;
        }
        changingKeybind = true;
        KeybindDialog dialog = new KeybindDialog(config.keybind());
        keybind.startRecording(dialog::onPressed);
        try {
            dialog.showAndWait().ifPresent(newKeybind -> {
                config = config.withKeybind(newKeybind);
                config.save();
                keybind.change(newKeybind);
                trayMenu.showKeybind(newKeybind);
            });
        } finally {
            keybind.stopRecording();
            changingKeybind = false;
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
