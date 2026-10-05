package io.github.phs314.quicktts.client;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 윈도우에 로그인하면 QuickTTS 가 자동으로 켜지게 한다.
 * 현재 사용자의 시작 프로그램 레지스트리(HKCU\...\Run)에 exe 경로를 넣고 뺀다.
 */
final class WindowsStartup {

    private static final String RUN_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String VALUE_NAME = "QuickTTS";

    private final Path exe;

    private WindowsStartup(Path exe) {
        this.exe = exe;
    }

    /** jpackage 로 만든 exe 로 윈도우에서 실행 중일 때만 쓸 수 있다. */
    static Optional<WindowsStartup> forThisApp() {
        String appPath = System.getProperty("jpackage.app-path");
        if (appPath == null || !System.getProperty("os.name").startsWith("Windows")) {
            return Optional.empty();
        }
        return Optional.of(new WindowsStartup(Path.of(appPath)));
    }

    boolean isEnabled() {
        return reg("query", RUN_KEY, "/v", VALUE_NAME);
    }

    /** @return 바꾸는 데 성공했으면 true */
    boolean setEnabled(boolean enabled) {
        if (enabled) {
            return reg("add", RUN_KEY, "/v", VALUE_NAME, "/t", "REG_SZ", "/d", quoted(exe), "/f");
        }
        return reg("delete", RUN_KEY, "/v", VALUE_NAME, "/f");
    }

    private static String quoted(Path path) {
        String value = path.toString();
        return value.contains(" ") ? "\\\"" + value + "\\\"" : value;
    }

    private static boolean reg(String... args) {
        List<String> command = new ArrayList<>();
        command.add("reg");
        command.addAll(List.of(args));
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
