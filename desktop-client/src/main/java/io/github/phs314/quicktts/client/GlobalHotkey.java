package io.github.phs314.quicktts.client;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 다른 프로그램을 쓰는 중에도 동작하는 전역 단축키. 지금은 Ctrl+Shift+Space 로 고정이다.
 */
class GlobalHotkey implements NativeKeyListener {

    private final Runnable onPressed;

    GlobalHotkey(Runnable onPressed) {
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

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        int modifiers = event.getModifiers();
        boolean ctrl = (modifiers & NativeKeyEvent.CTRL_MASK) != 0;
        boolean shift = (modifiers & NativeKeyEvent.SHIFT_MASK) != 0;
        if (ctrl && shift && event.getKeyCode() == NativeKeyEvent.VC_SPACE) {
            onPressed.run();
        }
    }
}
