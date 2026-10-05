package io.github.phs314.quicktts.client;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 사용자 홈의 {@code .discord-quick-tts/client.properties} 에서 읽는 클라이언트 설정.
 * 비밀값(API 키)은 저장소가 아니라 이 파일에만 둔다.
 */
public record ClientConfig(String serverUrl, String apiKey, String discordUserId) {

    public static final Path FILE = Path.of(System.getProperty("user.home"), ".discord-quick-tts", "client.properties");

    private static final String TEMPLATE = """
            # discord-quick-tts 데스크톱 클라이언트 설정
            # 봇 서버 주소
            server-url=http://localhost:8080
            # 봇 서버의 QUICKTTS_API_KEY 와 같은 값
            api-key=
            # 내 디스코드 사용자 ID (디스코드 설정 > 고급 > 개발자 모드를 켠 뒤 내 프로필에서 'ID 복사')
            discord-user-id=
            """;

    public boolean isComplete() {
        return !serverUrl.isBlank() && !apiKey.isBlank() && !discordUserId.isBlank();
    }

    /**
     * 설정 파일을 읽는다. 파일이 없으면 빈 템플릿을 만들어 둔다.
     */
    public static ClientConfig load() {
        try {
            if (Files.notExists(FILE)) {
                Files.createDirectories(FILE.getParent());
                Files.writeString(FILE, TEMPLATE, StandardCharsets.UTF_8);
            }
            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            return new ClientConfig(
                    properties.getProperty("server-url", "").strip(),
                    properties.getProperty("api-key", "").strip(),
                    properties.getProperty("discord-user-id", "").strip());
        } catch (IOException e) {
            throw new UncheckedIOException("설정 파일을 읽지 못했습니다: " + FILE, e);
        }
    }
}
