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
        Keybind keybind = Keybind.parse("Ctrl+Alt+T").orElseThrow();

        new ClientConfig("http://localhost:8080", "token", keybind).save(file);

        assertThat(ClientConfig.load(file)).isEqualTo(new ClientConfig("http://localhost:8080", "token", keybind));
    }

    @Test
    void 단축키가_없는_예전_설정_파일이면_기본_단축키를_쓴다() throws IOException {
        Path file = dir.resolve("client.properties");
        Files.writeString(file, "server-url=http://localhost:8080\ndevice-token=token\n", StandardCharsets.UTF_8);

        assertThat(ClientConfig.load(file).keybind()).isEqualTo(Keybind.DEFAULT);
    }

    @Test
    void 단축키를_잘못_적었으면_기본_단축키를_쓴다() throws IOException {
        Path file = dir.resolve("client.properties");
        Files.writeString(file, "device-token=token\nkeybind=Shift+A\n", StandardCharsets.UTF_8);

        assertThat(ClientConfig.load(file).keybind()).isEqualTo(Keybind.DEFAULT);
    }

    @Test
    void 설정_파일이_없으면_기본_단축키를_쓴다() {
        assertThat(ClientConfig.load(dir.resolve("missing.properties")).keybind()).isEqualTo(Keybind.DEFAULT);
    }

    @Test
    void 예전_hotkey_키로_적힌_단축키를_읽고_keybind_키로_옮겨_적는다() throws IOException {
        Path file = dir.resolve("client.properties");
        Files.writeString(file, "server-url=http://localhost:8080\ndevice-token=token\nhotkey=Ctrl+Alt+T\n",
                StandardCharsets.UTF_8);

        assertThat(ClientConfig.load(file).keybind()).isEqualTo(Keybind.parse("Ctrl+Alt+T").orElseThrow());
        assertThat(Files.readString(file, StandardCharsets.UTF_8))
                .contains("keybind=Ctrl+Alt+T")
                .doesNotContain("hotkey=");
    }

    @Test
    void keybind_키가_있으면_예전_hotkey_키는_보지_않는다() throws IOException {
        Path file = dir.resolve("client.properties");
        Files.writeString(file, "device-token=token\nkeybind=F9\nhotkey=Ctrl+Alt+T\n", StandardCharsets.UTF_8);

        assertThat(ClientConfig.load(file).keybind()).isEqualTo(Keybind.parse("F9").orElseThrow());
    }
}
