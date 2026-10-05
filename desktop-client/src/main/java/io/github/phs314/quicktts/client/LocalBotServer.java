package io.github.phs314.quicktts.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * 이 PC 에서 도는 봇 서버를 클라이언트와 함께 켜고 끈다.
 * exe 폴더에 {@code QuickTTS-Server.exe} 가 있고 서버 주소가 이 PC 일 때만 동작한다.
 * 서버가 이미 떠 있으면(직접 켰거나 개발 중이면) 건드리지 않는다.
 */
final class LocalBotServer implements AutoCloseable {

    private static final String SERVER_EXE = "QuickTTS-Server.exe";
    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "[::1]");

    private final Process process;

    private LocalBotServer(Process process) {
        this.process = process;
    }

    /**
     * 필요하면 봇 서버를 켠다. 켜지는 데는 몇 초 걸리므로 기다리지 않고 바로 돌아온다.
     */
    static LocalBotServer startIfNeeded(String serverUrl) {
        Optional<Path> serverExe = serverExe();
        if (serverExe.isEmpty() || !isLocal(serverUrl) || isRunning(serverUrl)) {
            return new LocalBotServer(null);
        }
        try {
            Process process = new ProcessBuilder(serverExe.get().toString())
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            return new LocalBotServer(process);
        } catch (IOException e) {
            System.err.println("봇 서버를 켜지 못했습니다: " + e.getMessage());
            return new LocalBotServer(null);
        }
    }

    /** 내가 켠 서버만 끈다. exe 실행기 아래에 실제 자바 프로세스가 있으므로 함께 끈다. */
    @Override
    public void close() {
        if (process == null) {
            return;
        }
        process.descendants().forEach(ProcessHandle::destroy);
        process.destroy();
    }

    /** jpackage 로 만든 exe 에서 실행 중이면 같은 폴더의 서버 실행기를 찾는다. */
    private static Optional<Path> serverExe() {
        String appPath = System.getProperty("jpackage.app-path");
        if (appPath == null) {
            return Optional.empty();
        }
        Path exe = Path.of(appPath).resolveSibling(SERVER_EXE);
        return Files.isRegularFile(exe) ? Optional.of(exe) : Optional.empty();
    }

    private static boolean isLocal(String serverUrl) {
        String host = URI.create(serverUrl).getHost();
        return host != null && LOCAL_HOSTS.contains(host.toLowerCase());
    }

    /** 무슨 응답이든 오면 떠 있는 것이다. */
    private static boolean isRunning(String serverUrl) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(serverUrl + "/"))
                .timeout(Duration.ofSeconds(1))
                .GET()
                .build();
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build()) {
            client.send(request, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
