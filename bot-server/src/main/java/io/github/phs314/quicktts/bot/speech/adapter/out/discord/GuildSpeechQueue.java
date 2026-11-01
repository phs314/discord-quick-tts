package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 서버(길드) 하나의 음성 재생 순서. 앞 문장이 끝나야 다음 문장을 읽는다.
 *
 * <p>LavaPlayer 는 자기 잠금을 쥔 채로 {@link #onTrackEnd} 를 부르고, 이 큐는 자기 잠금을 쥔 채로 LavaPlayer 를 부른다.
 * {@link #onTrackEnd} 에서 이 큐의 잠금까지 잡으면 두 잠금을 반대 순서로 기다리다 둘 다 멈출 수 있으므로,
 * 다음 문장을 트는 일은 다른 스레드에 넘긴다.
 */
class GuildSpeechQueue extends AudioEventAdapter {

    private final AudioPlayer player;
    private final Executor startNextExecutor;
    /** 이 큐의 잠금 안에서만 만진다. */
    private final Queue<AudioTrack> pending = new ArrayDeque<>();

    GuildSpeechQueue(AudioPlayer player) {
        this(player, CompletableFuture::runAsync);
    }

    GuildSpeechQueue(AudioPlayer player, Executor startNextExecutor) {
        this.player = player;
        this.startNextExecutor = startNextExecutor;
        player.addListener(this);
    }

    AudioPlayer player() {
        return player;
    }

    synchronized void enqueue(AudioTrack track) {
        pending.offer(track);
        startNextIfIdle();
    }

    /** 읽고 있던 문장을 끊고 기다리던 문장도 모두 버린다. */
    synchronized void clear() {
        pending.clear();
        player.stopTrack();
    }

    /** 기다리던 문장을 버리고 플레이어를 없앤다. 그 뒤로는 이 큐를 쓰지 않는다. */
    synchronized void destroy() {
        pending.clear();
        player.destroy();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            startNextExecutor.execute(this::startNextIfIdle);
        }
    }

    /** 재생 중인 문장이 없으면 가장 먼저 온 문장을 튼다. 재생 중이면 그 문장이 끝난 뒤에 다시 불린다. */
    private synchronized void startNextIfIdle() {
        AudioTrack next = pending.peek();
        if (next != null && player.startTrack(next, true)) {
            pending.poll();
        }
    }
}
