package io.github.phs314.quicktts.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * QuickTTS 를 한 번만 띄운다. 먼저 뜬 앱이 이 PC 안에서만 닿는 포트를 잡고 기다리다가,
 * 나중에 실행된 앱이 "입력창 열어 줘" 라고 알리면 입력창을 연다. 나중에 실행된 앱은 그대로 끝난다.
 */
final class SingleInstance implements AutoCloseable {

    private static final int PORT = 47613;
    private static final String SHOW_REQUEST = "quicktts-show";
    private static final String SHOW_ACCEPTED = "quicktts-ok";
    private static final int TIMEOUT_MILLIS = 1000;

    private final ServerSocket server;
    private volatile Runnable onShowRequested = () -> { };

    private SingleInstance(ServerSocket server) {
        this.server = server;
        if (server != null) {
            Thread.ofPlatform().name("quicktts-single-instance").daemon().start(this::acceptLoop);
        }
    }

    /**
     * 이 프로세스가 첫 번째 QuickTTS 면 자리를 잡아 돌려준다.
     * 이미 떠 있는 QuickTTS 가 있으면 그쪽에 입력창을 열라고 알리고 null 을 돌려준다.
     */
    static SingleInstance claim() {
        try {
            return new SingleInstance(new ServerSocket(PORT, 10, InetAddress.getLoopbackAddress()));
        } catch (IOException portInUse) {
            if (askRunningInstanceToShow()) {
                return null;
            }
            // 다른 프로그램이 같은 포트를 쓰고 있다. 중복 실행 확인 없이 그냥 실행한다.
            return new SingleInstance(null);
        }
    }

    void onShowRequested(Runnable action) {
        this.onShowRequested = action;
    }

    @Override
    public void close() {
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                // 종료 중이므로 무시한다.
            }
        }
    }

    private static boolean askRunningInstanceToShow() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), PORT), TIMEOUT_MILLIS);
            socket.setSoTimeout(TIMEOUT_MILLIS);
            new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8).println(SHOW_REQUEST);
            String reply = reader(socket).readLine();
            return SHOW_ACCEPTED.equals(reply);
        } catch (IOException e) {
            return false;
        }
    }

    private void acceptLoop() {
        while (!server.isClosed()) {
            try (Socket socket = server.accept()) {
                socket.setSoTimeout(TIMEOUT_MILLIS);
                if (SHOW_REQUEST.equals(reader(socket).readLine())) {
                    new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8).println(SHOW_ACCEPTED);
                    onShowRequested.run();
                }
            } catch (IOException e) {
                // 서버를 닫았거나 이상한 연결이다. 닫혔으면 반복문이 끝난다.
            }
        }
    }

    private static BufferedReader reader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }
}
