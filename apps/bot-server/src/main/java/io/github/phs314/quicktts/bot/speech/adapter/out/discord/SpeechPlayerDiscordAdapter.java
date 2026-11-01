package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetection;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetectionResult;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerHints;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import io.github.phs314.quicktts.bot.speech.application.exception.BotCannotJoinVoiceChannelException;
import io.github.phs314.quicktts.bot.speech.application.exception.UnplayableAudioException;
import io.github.phs314.quicktts.bot.speech.application.port.out.SpeechPlayerPort;
import io.github.phs314.quicktts.bot.speech.domain.vo.Audio;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 디스코드 음성 채널에 들어가고 나가며, 음성 바이트를 메모리에서 바로 LavaPlayer 트랙으로 만들어 재생한다.
 * 파일을 거치지 않으므로 트랙은 부른 자리에서 바로 재생 순서에 들어간다.
 */
@Component
public class SpeechPlayerDiscordAdapter implements SpeechPlayerPort {

    private static final Logger log = LoggerFactory.getLogger(SpeechPlayerDiscordAdapter.class);

    /** 형식을 알아낼 때 LavaPlayer 가 로그와 트랙 정보에 쓰는 이름. 메모리의 음성이라 주소는 없다. */
    private static final AudioReference SPEECH = new AudioReference("quicktts-speech", "Quick TTS");

    private final ShardManager shardManager;
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final Map<GuildId, GuildSpeechQueue> queues = new ConcurrentHashMap<>();

    public SpeechPlayerDiscordAdapter(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    /** 봇 서버가 꺼질 때 LavaPlayer 의 재생 스레드를 멈춘다. */
    @PreDestroy
    void shutdown() {
        playerManager.shutdown();
    }

    @Override
    public void join(VoiceChannel channel) {
        Guild guild = shardManager.getGuildById(channel.guildId().value());
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

    /**
     * @throws UnplayableAudioException 음성 바이트가 LavaPlayer 가 읽을 수 있는 형식이 아닐 때
     */
    @Override
    public void play(GuildId guildId, Audio audio) {
        queueOf(guildId).enqueue(toTrack(audio));
    }

    @Override
    public void leave(GuildId guildId) {
        GuildSpeechQueue queue = queues.get(guildId);
        if (queue != null) {
            queue.clear();
        }
        Guild guild = shardManager.getGuildById(guildId.value());
        // 아직 들어가는 중이라 isConnected() 가 false 여도 닫아야 들어가기가 취소된다. 연결이 없으면 아무 일도 없다.
        if (guild != null) {
            guild.getAudioManager().closeAudioConnection();
        }
    }

    /**
     * 디스코드는 봇이 나간 디스코드 서버의 음성 연결을 닫고 {@link AudioManager} 도 버린다. 다시 초대되면 새 AudioManager 에
     * 새 플레이어를 붙이므로, 옛 플레이어와 남은 문장은 여기서 버린다. 남겨 두면 다시 들어갔을 때 옛 문장이 읽힌다.
     */
    @Override
    public void forget(GuildId guildId) {
        GuildSpeechQueue queue = queues.remove(guildId);
        if (queue != null) {
            queue.destroy();
        }
    }

    private GuildSpeechQueue queueOf(GuildId guildId) {
        return queues.computeIfAbsent(guildId, id -> new GuildSpeechQueue(playerManager.createPlayer()));
    }

    /**
     * 음성 바이트의 형식(mp3 등)을 알아내 그 형식의 트랙을 만든다. LavaPlayer 의 파일 소스가 하는 일과 같고 스트림만 메모리다.
     * 형식을 알아낼 때 스트림을 앞에서부터 읽으므로 재생에는 새 스트림을 준다.
     */
    static AudioTrack toTrack(Audio audio) {
        MediaContainerDetectionResult result;
        try {
            result = new MediaContainerDetection(MediaContainerRegistry.DEFAULT_REGISTRY, SPEECH,
                    new ByteArraySeekableInputStream(audio.content()),
                    MediaContainerHints.from(null, audio.format())).detectContainer();
        } catch (FriendlyException e) {
            throw new UnplayableAudioException("음성 형식을 알아내지 못했습니다.", e);
        }
        if (!result.isContainerDetected() || result.isReference() || !result.isSupportedFile()) {
            throw new UnplayableAudioException("재생할 수 없는 음성 형식입니다: " + audio.format());
        }
        return result.getContainerDescriptor()
                .createTrack(result.getTrackInfo(), new ByteArraySeekableInputStream(audio.content()));
    }
}
