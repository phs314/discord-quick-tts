package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpeechPlayerDiscordAdapterTest {

    private static final Instant NOW = Instant.parse("2026-10-18T12:00:00Z");

    @TempDir
    Path directory;

    @Test
    void 켜질_때_오래된_임시_음성_파일만_지운다() throws IOException {
        Path leftover = fileModifiedAt("speech-old.mp3", NOW.minus(SpeechPlayerDiscordAdapter.LEFTOVER_AGE).minusSeconds(1));
        Path playing = fileModifiedAt("speech-new.mp3", NOW.minusSeconds(10));

        SpeechPlayerDiscordAdapter.deleteLeftovers(directory, NOW.minus(SpeechPlayerDiscordAdapter.LEFTOVER_AGE));

        assertThat(leftover).doesNotExist();
        assertThat(playing).exists();
    }

    private Path fileModifiedAt(String name, Instant modifiedAt) throws IOException {
        Path file = Files.writeString(directory.resolve(name), "mp3");
        Files.setLastModifiedTime(file, FileTime.from(modifiedAt));
        return file;
    }
}
