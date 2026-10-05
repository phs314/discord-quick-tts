package io.github.phs314.quicktts.client;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.util.Optional;

/**
 * 작업 표시줄 트레이 아이콘. 종료, 윈도우 시작 시 실행 메뉴와 전송 실패 알림을 맡는다.
 */
class TrayMenu {

    private TrayIcon trayIcon;

    void install(Runnable onExit, Optional<WindowsStartup> startup) {
        if (!SystemTray.isSupported()) {
            return;
        }
        EventQueue.invokeLater(() -> {
            PopupMenu menu = new PopupMenu();
            startup.ifPresent(windowsStartup -> menu.add(startWithWindowsItem(windowsStartup)));
            MenuItem exit = new MenuItem("Exit");
            exit.addActionListener(e -> onExit.run());
            menu.add(exit);

            trayIcon = new TrayIcon(createIcon(), "Discord Quick TTS (Ctrl+Shift+Space)", menu);
            trayIcon.setImageAutoSize(true);
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                trayIcon = null;
            }
        });
    }

    void showError(String message) {
        EventQueue.invokeLater(() -> {
            if (trayIcon != null) {
                trayIcon.displayMessage("Discord Quick TTS", message, TrayIcon.MessageType.ERROR);
            } else {
                System.err.println(message);
            }
        });
    }

    void remove() {
        EventQueue.invokeLater(() -> {
            if (trayIcon != null) {
                SystemTray.getSystemTray().remove(trayIcon);
            }
        });
    }

    /** 윈도우에 로그인하면 QuickTTS(와 이 PC 의 봇 서버)가 같이 켜지게 하는 켜기/끄기 메뉴. */
    private CheckboxMenuItem startWithWindowsItem(WindowsStartup startup) {
        CheckboxMenuItem item = new CheckboxMenuItem("Start with Windows", startup.isEnabled());
        item.addItemListener(event -> {
            boolean wanted = item.getState();
            if (!startup.setEnabled(wanted)) {
                item.setState(!wanted);
                showError("윈도우 시작 프로그램 설정을 바꾸지 못했습니다.");
            }
        });
        return item;
    }

    private static Image createIcon() {
        return Toolkit.getDefaultToolkit().getImage(TrayMenu.class.getResource("tray-icon.png"));
    }
}
