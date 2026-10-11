package io.github.phs314.quicktts.client;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * 작업 표시줄 트레이 아이콘. 종료, 단축키 바꾸기, 윈도우 시작 시 실행 메뉴와 전송 실패·새 버전 알림을 맡는다.
 */
class TrayMenu {

    private TrayIcon trayIcon;

    void install(Hotkey hotkey, Runnable onChangeHotkey, Runnable onExit, Optional<WindowsStartup> startup) {
        if (!SystemTray.isSupported()) {
            return;
        }
        EventQueue.invokeLater(() -> {
            PopupMenu menu = new PopupMenu();
            MenuItem changeHotkey = new MenuItem("Change Shortcut...");
            changeHotkey.addActionListener(e -> onChangeHotkey.run());
            menu.add(changeHotkey);
            startup.ifPresent(windowsStartup -> menu.add(startWithWindowsItem(windowsStartup)));
            MenuItem exit = new MenuItem("Exit");
            exit.addActionListener(e -> onExit.run());
            menu.add(exit);

            trayIcon = new TrayIcon(createIcon(), tooltip(hotkey), menu);
            trayIcon.setImageAutoSize(true);
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                trayIcon = null;
            }
        });
    }

    /** 트레이 아이콘에 마우스를 올리면 보이는 단축키 안내를 바꾼다. */
    void showHotkey(Hotkey hotkey) {
        EventQueue.invokeLater(() -> {
            if (trayIcon != null) {
                trayIcon.setToolTip(tooltip(hotkey));
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

    /**
     * 새 버전이 나왔다고 알리고, 알림을 누르거나 메뉴의 Download 를 누르면 Release 페이지를 연다.
     */
    void showUpdate(UpdateChecker.Release release) {
        EventQueue.invokeLater(() -> {
            if (trayIcon == null) {
                return;
            }
            MenuItem download = new MenuItem("Download v" + release.version() + "...");
            download.addActionListener(e -> openInBrowser(release.page()));
            trayIcon.getPopupMenu().insert(download, 0);
            // 윈도우에서는 알림을 누르거나 트레이 아이콘을 두 번 누르면 불린다.
            trayIcon.addActionListener(e -> openInBrowser(release.page()));
            trayIcon.displayMessage("Discord Quick TTS",
                    "새 버전 " + release.version() + " 이 나왔습니다. 눌러서 내려받으세요.", TrayIcon.MessageType.INFO);
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

    private void openInBrowser(URI page) {
        try {
            Desktop.getDesktop().browse(page);
        } catch (IOException | UnsupportedOperationException e) {
            showError("브라우저를 열지 못했습니다: " + page);
        }
    }

    private static String tooltip(Hotkey hotkey) {
        return "Discord Quick TTS (" + hotkey + ")";
    }

    private static Image createIcon() {
        return Toolkit.getDefaultToolkit().getImage(TrayMenu.class.getResource("tray-icon.png"));
    }
}
