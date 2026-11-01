package io.github.phs314.quicktts.client;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 사용자 홈의 {@code .discord-quick-tts/client.properties} 에 저장하는 클라이언트 설정.
 * 기기 토큰은 디스코드 {@code /연결} 코드로 등록할 때 앱이 직접 채운다.
 */
public record ClientConfig(String serverUrl, String deviceToken, Keybind keybind) {

    public static final Path FILE = Path.of(System.getProperty("user.home"), ".discord-quick-tts", "client.properties");

    /** 배포할 때는 {@code -Dquicktts.server-url=...} 로 기본 서버 주소를 바꾼다. */
    private static final String DEFAULT_SERVER_URL = System.getProperty("quicktts.server-url", "http://localhost:8080");

    /** 예전 판이 단축키를 적던 키. 읽으면 {@code keybind=} 로 옮겨 적는다. */
    private static final String LEGACY_KEYBIND_KEY = "hotkey";

    private static final String TEMPLATE = """
            # discord-quick-tts 데스크톱 클라이언트 설정
            # 봇 서버 주소
            server-url=%s
            # 이 PC 의 기기 토큰. 앱이 디스코드 /연결 코드로 등록할 때 채운다. 다른 사람에게 보여 주지 마세요.
            device-token=%s
            # 입력창을 여는 단축키. 트레이 메뉴의 Change Keybind... 에서 바꾼다. 예: Ctrl+Shift+Space, Ctrl+Alt+T, F9
            keybind=%s
            """;

    public boolean isRegistered() {
        return !deviceToken.isBlank();
    }

    public ClientConfig withDeviceToken(String newDeviceToken) {
        return new ClientConfig(serverUrl, newDeviceToken, keybind);
    }

    public ClientConfig withKeybind(Keybind newKeybind) {
        return new ClientConfig(serverUrl, deviceToken, newKeybind);
    }

    public static ClientConfig load() {
        return load(FILE);
    }

    /**
     * 단축키가 없거나 잘못 적혀 있으면 기본 단축키를 쓴다.
     * 예전 판의 {@code hotkey=} 만 있으면 그 값을 읽고 파일을 {@code keybind=} 로 다시 적는다.
     */
    static ClientConfig load(Path file) {
        if (Files.notExists(file)) {
            return new ClientConfig(DEFAULT_SERVER_URL, "", Keybind.DEFAULT);
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("설정 파일을 읽지 못했습니다: " + file, e);
        }
        String serverUrl = properties.getProperty("server-url", "").strip();
        String keybind = properties.getProperty("keybind");
        boolean legacy = keybind == null && properties.containsKey(LEGACY_KEYBIND_KEY);
        if (legacy) {
            keybind = properties.getProperty(LEGACY_KEYBIND_KEY);
        }
        ClientConfig config = new ClientConfig(
                serverUrl.isEmpty() ? DEFAULT_SERVER_URL : serverUrl,
                properties.getProperty("device-token", "").strip(),
                Keybind.parse(keybind == null ? "" : keybind).orElse(Keybind.DEFAULT));
        if (legacy) {
            config.save(file);
        }
        return config;
    }

    public void save() {
        save(FILE);
    }

    void save(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, TEMPLATE.formatted(serverUrl, deviceToken, keybind), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("설정 파일을 저장하지 못했습니다: " + file, e);
        }
    }
}
