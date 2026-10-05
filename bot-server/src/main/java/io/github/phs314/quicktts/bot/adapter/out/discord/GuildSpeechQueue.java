package io.github.phs314.quicktts.bot.adapter.out.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 서버(길드) 하나의 음성 재생 순서. 앞 문장이 끝나야 다음 문장을 읽는다.
 */
class GuildSpeechQueue extends AudioEventAdapter {

    private final AudioPlayer player;
    private final Queue<AudioTrack> pending = new ConcurrentLinkedQueue<>();

    GuildSpeechQueue(AudioPlayer player) {
        this.player = player;
        player.addListener(this);
    }

    AudioPlayer player() {
        return player;
    }

    synchronized void enqueue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            pending.offer(track);
        }
    }

    @Override
    public synchronized void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (track.getUserData() instanceof Path file) {
            LavaPlayerSpeechPlayer.deleteQuietly(file);
        }
        if (endReason.mayStartNext) {
            player.startTrack(pending.poll(), false);
        }
    }
}
