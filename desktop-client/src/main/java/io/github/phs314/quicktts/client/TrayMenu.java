package io.github.phs314.quicktts.client;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

/**
 * 작업 표시줄 트레이 아이콘. 종료 메뉴와 전송 실패 알림을 맡는다.
 */
class TrayMenu {

    private TrayIcon trayIcon;

    void install(Runnable onExit) {
        if (!SystemTray.isSupported()) {
            return;
        }
        EventQueue.invokeLater(() -> {
            PopupMenu menu = new PopupMenu();
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

    private static BufferedImage createIcon() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x5865F2));
        g.fillOval(0, 0, 32, 32);
        g.dispose();
        return image;
    }
}
