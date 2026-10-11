package io.github.phs314.quicktts.client;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 다른 프로그램을 쓰는 중에도 동작하는 전역 단축키. 단축키는 설정 파일에서 읽고 트레이 메뉴에서 바꾼다.
 */
class GlobalHotkey implements NativeKeyListener {

    private final Runnable onPressed;
    private volatile Hotkey hotkey;
    /** 새 단축키를 받는 중이면 키 입력을 여기로 넘기고 입력창은 띄우지 않는다. */
    private volatile Consumer<Hotkey> recorder;

    GlobalHotkey(Hotkey hotkey, Runnable onPressed) {
        this.hotkey = hotkey;
        this.onPressed = onPressed;
    }

    void register() throws NativeHookException {
        // JNativeHook 은 기본적으로 모든 키 입력을 로그로 남기므로 줄인다.
        Logger hookLogger = Logger.getLogger(GlobalScreen.class.getPackageName());
        hookLogger.setLevel(Level.WARNING);
        hookLogger.setUseParentHandlers(false);

        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(this);
    }

    void unregister() {
        GlobalScreen.removeNativeKeyListener(this);
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            // 종료 중이므로 무시한다.
        }
    }

    void change(Hotkey newHotkey) {
        hotkey = newHotkey;
    }

    /**
     * 단축키 바꾸기 창이 떠 있는 동안 누른 키를 받는다. 입력창을 여는 키와 같은 경로로 받아서
     * 저장한 단축키가 나중에 그대로 눌린다. 콜백은 JNativeHook 스레드에서 불린다.
     */
    void startRecording(Consumer<Hotkey> onKeyPressed) {
        recorder = onKeyPressed;
    }

    void stopRecording() {
        recorder = null;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        Consumer<Hotkey> currentRecorder = recorder;
        if (currentRecorder != null) {
            Hotkey.fromEvent(event.getModifiers(), event.getKeyCode()).ifPresent(currentRecorder);
            return;
        }
        if (hotkey.matches(event.getModifiers(), event.getKeyCode())) {
            onPressed.run();
        }
    }
}
