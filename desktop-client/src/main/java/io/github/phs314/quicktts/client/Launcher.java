package io.github.phs314.quicktts.client;

import javafx.application.Application;

/**
 * JavaFX {@link Application} 을 상속하지 않는 진입점. 클래스패스로 실행해도 JavaFX 가 뜨게 한다.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(QuickChatApp.class, args);
    }
}
