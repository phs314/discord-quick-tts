package io.github.phs314.quicktts.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * client.properties 에 단축키를 저장하고 다시 읽는지 확인한다. 사용자 홈 대신 임시 폴더를 쓴다.
 */
class ClientConfigTest {

    @TempDir
    Path dir;

    @Test
    void 저장한_단축키를_다시_읽는다() {
        Path file = dir.resolve("client.properties");
        Hotkey hotkey = Hotkey.parse("Ctrl+Alt+T").orElseThrow();

        new ClientConfig("http://localhost:8080", "token", hotkey).save(file);

        assertThat(ClientConfig.load(file)).isEqualTo(new ClientConfig("http://localhost:8080", "token", hotkey));
    }

    @Test
    void 단축키가_없는_예전_설정_파일이면_기본_단축키를_쓴다() throws IOException {
        Path file = dir.resolve("client.properties");
        Files.writeString(file, "server-url=http://localhost:8080\ndevice-token=token\n", StandardCharsets.UTF_8);

        assertThat(ClientConfig.load(file).hotkey()).isEqualTo(Hotkey.DEFAULT);
    }

    @Test
    void 단축키를_잘못_적었으면_기본_단축키를_쓴다() throws IOException {
        Path file = dir.resolve("client.properties");
        Files.writeString(file, "device-token=token\nhotkey=Shift+A\n", StandardCharsets.UTF_8);

        assertThat(ClientConfig.load(file).hotkey()).isEqualTo(Hotkey.DEFAULT);
    }

    @Test
    void 설정_파일이_없으면_기본_단축키를_쓴다() {
        assertThat(ClientConfig.load(dir.resolve("missing.properties")).hotkey()).isEqualTo(Hotkey.DEFAULT);
    }
}
