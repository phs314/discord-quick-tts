package io.github.phs314.quicktts.client;

import javafx.application.Application;

/**
 * JavaFX {@link Application} 을 상속하지 않는 진입점. 클래스패스로 실행해도 JavaFX 가 뜨게 한다.
 * QuickTTS 가 이미 떠 있으면 새로 띄우지 않고 떠 있는 쪽의 입력창을 연다.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        SingleInstance instance = SingleInstance.claim();
        if (instance == null) {
            return;
        }
        QuickChatApp.singleInstance = instance;
        Application.launch(QuickChatApp.class, args);
    }
}
