package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.exception.BotCannotJoinVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.Speech;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 디스코드 음성 채널에 들어가고 나가며, 음성을 임시 파일로 저장한 뒤 LavaPlayer 로 재생한다.
 * 임시 파일은 시스템 임시 폴더 아래 {@code quicktts} 폴더에 두고, 다 읽으면 지운다. 봇 서버가 강제로 꺼져 남은 파일은
 * 다음에 켜질 때 지운다.
 */
@Component
public class SpeechPlayerDiscordAdapter implements SpeechPlayerPort {

    private static final Logger log = LoggerFactory.getLogger(SpeechPlayerDiscordAdapter.class);

    /** 켜질 때 이보다 오래된 임시 파일만 지운다. 같은 PC 에서 도는 다른 봇 서버가 읽고 있는 파일은 건드리지 않기 위해서다. */
    static final Duration LEFTOVER_AGE = Duration.ofHours(1);

    private final JDA jda;
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final Map<GuildId, GuildSpeechQueue> queues = new ConcurrentHashMap<>();
    private final Path audioDirectory = Path.of(System.getProperty("java.io.tmpdir"), "quicktts");

    public SpeechPlayerDiscordAdapter(JDA jda, Clock clock) {
        this.jda = jda;
        AudioSourceManagers.registerLocalSource(playerManager);
        try {
            Files.createDirectories(audioDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("임시 음성 파일 폴더를 만들지 못했습니다: " + audioDirectory, e);
        }
        deleteLeftovers(audioDirectory, clock.instant().minus(LEFTOVER_AGE));
    }

    /** 봇 서버가 꺼질 때 LavaPlayer 의 재생 스레드를 멈춘다. */
    @PreDestroy
    void shutdown() {
        playerManager.shutdown();
    }

    @Override
    public void join(VoiceChannel channel) {
        Guild guild = jda.getGuildById(channel.guildId().value());
        AudioChannel audioChannel = guild == null ? null : guild.getChannelById(AudioChannel.class, channel.channelId());
        if (audioChannel == null) {
            log.warn("음성 채널을 찾을 수 없습니다: {}", channel);
            return;
        }
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.getSendingHandler() == null) {
            audioManager.setSendingHandler(new AudioPlayerSendHandler(queueOf(channel.guildId()).player()));
        }
        if (!audioChannel.equals(audioManager.getConnectedChannel())) {
            try {
                audioManager.openAudioConnection(audioChannel);
            } catch (InsufficientPermissionException e) {
                throw new BotCannotJoinVoiceChannelException(channel, e);
            }
        }
    }

    @Override
    public void play(GuildId guildId, Speech speech) {
        GuildSpeechQueue queue = queueOf(guildId);
        Path audioFile = writeTempFile(speech);
        // 파일 읽기는 여러 스레드에서 돌므로, 디스코드 서버마다 넣은 순서대로 끝나게 해야 문장 순서가 바뀌지 않는다.
        playerManager.loadItemOrdered(guildId, audioFile.toString(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(audioFile);
                queue.enqueue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                log.warn("음성 파일이 재생 목록으로 읽혔습니다: {}", audioFile);
                deleteQuietly(audioFile);
            }

            @Override
            public void noMatches() {
                log.warn("음성 파일을 읽을 수 없습니다: {}", audioFile);
                deleteQuietly(audioFile);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                log.warn("음성 파일을 불러오지 못했습니다: {}", audioFile, e);
                deleteQuietly(audioFile);
            }
        });
    }

    @Override
    public void leave(GuildId guildId) {
        GuildSpeechQueue queue = queues.get(guildId);
        if (queue != null) {
            queue.clear();
        }
        Guild guild = jda.getGuildById(guildId.value());
        // 아직 들어가는 중이라 isConnected() 가 false 여도 닫아야 들어가기가 취소된다. 연결이 없으면 아무 일도 없다.
        if (guild != null) {
            guild.getAudioManager().closeAudioConnection();
        }
    }

    private GuildSpeechQueue queueOf(GuildId guildId) {
        return queues.computeIfAbsent(guildId, id -> new GuildSpeechQueue(playerManager.createPlayer()));
    }

    private Path writeTempFile(Speech speech) {
        try {
            Path file = Files.createTempFile(audioDirectory, "speech-", "." + speech.fileExtension());
            return Files.write(file, speech.audio());
        } catch (IOException e) {
            throw new UncheckedIOException("임시 음성 파일을 만들지 못했습니다.", e);
        }
    }

    /** 폴더 안에서 {@code before} 전에 마지막으로 고친 파일을 지운다. */
    static void deleteLeftovers(Path directory, Instant before) {
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(file -> isModifiedBefore(file, before)).forEach(SpeechPlayerDiscordAdapter::deleteQuietly);
        } catch (IOException e) {
            log.warn("남은 임시 음성 파일을 확인하지 못했습니다: {}", directory, e);
        }
    }

    private static boolean isModifiedBefore(Path file, Instant before) {
        try {
            return Files.isRegularFile(file) && Files.getLastModifiedTime(file).toInstant().isBefore(before);
        } catch (IOException e) {
            return false;
        }
    }

    static void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("임시 음성 파일을 지우지 못했습니다: {}", file, e);
        }
    }
}
