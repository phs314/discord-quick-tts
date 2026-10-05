package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayer;
import io.github.phs314.quicktts.bot.speech.domain.Speech;
import io.github.phs314.quicktts.bot.speech.domain.VoiceChannel;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 음성을 임시 파일로 저장한 뒤 LavaPlayer 로 디스코드 음성 채널에 재생한다.
 */
@Component
public class LavaPlayerSpeechPlayer implements SpeechPlayer {

    private static final Logger log = LoggerFactory.getLogger(LavaPlayerSpeechPlayer.class);

    private final JDA jda;
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final Map<Long, GuildSpeechQueue> queues = new ConcurrentHashMap<>();

    public LavaPlayerSpeechPlayer(JDA jda) {
        this.jda = jda;
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    @Override
    public void play(VoiceChannel channel, Speech speech) {
        Guild guild = jda.getGuildById(channel.guildId());
        AudioChannel audioChannel = guild == null ? null : guild.getChannelById(AudioChannel.class, channel.channelId());
        if (audioChannel == null) {
            log.warn("음성 채널을 찾을 수 없습니다: {}", channel);
            return;
        }

        GuildSpeechQueue queue = queues.computeIfAbsent(guild.getIdLong(),
                id -> new GuildSpeechQueue(playerManager.createPlayer()));
        connect(guild, audioChannel, queue);

        Path audioFile = writeTempFile(speech);
        playerManager.loadItem(audioFile.toString(), new AudioLoadResultHandler() {
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

    /** 읽던 문장을 모두 버리고 음성 채널에서 나간다. */
    void leave(long guildId) {
        GuildSpeechQueue queue = queues.get(guildId);
        if (queue != null) {
            queue.clear();
        }
        Guild guild = jda.getGuildById(guildId);
        if (guild != null && guild.getAudioManager().isConnected()) {
            guild.getAudioManager().closeAudioConnection();
        }
    }

    private static void connect(Guild guild, AudioChannel channel, GuildSpeechQueue queue) {
        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.getSendingHandler() == null) {
            audioManager.setSendingHandler(new AudioPlayerSendHandler(queue.player()));
        }
        if (!channel.equals(audioManager.getConnectedChannel())) {
            audioManager.openAudioConnection(channel);
        }
    }

    private static Path writeTempFile(Speech speech) {
        try {
            Path file = Files.createTempFile("quicktts-", "." + speech.fileExtension());
            return Files.write(file, speech.audio());
        } catch (IOException e) {
            throw new UncheckedIOException("임시 음성 파일을 만들지 못했습니다.", e);
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
