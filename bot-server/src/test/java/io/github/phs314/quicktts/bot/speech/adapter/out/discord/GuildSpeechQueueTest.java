package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GuildSpeechQueueTest {

    private final AudioPlayer player = mock(AudioPlayer.class);
    private final GuildSpeechQueue queue = new GuildSpeechQueue(player, Runnable::run);

    @Test
    void 재생_중이면_기다렸다가_앞_문장이_끝나면_다음_문장을_읽는다() {
        AudioTrack first = mock(AudioTrack.class);
        AudioTrack second = mock(AudioTrack.class);
        when(player.startTrack(first, true)).thenReturn(true);
        when(player.startTrack(second, true)).thenReturn(false, true);

        queue.enqueue(first);
        queue.enqueue(second);
        queue.onTrackEnd(player, first, AudioTrackEndReason.FINISHED);

        verify(player, times(2)).startTrack(second, true);
        verify(player, never()).startTrack(any(), eq(false));
    }

    @Test
    void 비우면_재생을_멈추고_기다리던_문장도_읽지_않는다() {
        AudioTrack playing = mock(AudioTrack.class);
        AudioTrack waiting = mock(AudioTrack.class);
        when(player.startTrack(playing, true)).thenReturn(true);
        when(player.startTrack(waiting, true)).thenReturn(false);
        queue.enqueue(playing);
        queue.enqueue(waiting);

        queue.clear();
        queue.onTrackEnd(player, playing, AudioTrackEndReason.STOPPED);

        verify(player).stopTrack();
        verify(player, times(1)).startTrack(waiting, true);
    }

    @Test
    void 다음_문장을_못_틀고_멈췄어도_새_문장이_오면_먼저_온_문장부터_읽는다() {
        AudioTrack older = mock(AudioTrack.class);
        AudioTrack newer = mock(AudioTrack.class);
        // 다른 문장이 재생 중이라 기다리다가, 그 문장이 다음 문장을 틀지 않는 이유(CLEANUP 등)로 끝난 경우
        when(player.startTrack(older, true)).thenReturn(false, true);
        when(player.startTrack(newer, true)).thenReturn(false);
        queue.enqueue(older);

        queue.enqueue(newer);

        verify(player, times(2)).startTrack(older, true);
        verify(player, never()).startTrack(newer, true);
    }

    @Test
    void 재생이_끝났다는_알림은_큐의_잠금을_기다리지_않는다() throws InterruptedException {
        // LavaPlayer 는 자기 잠금을 쥔 채로 onTrackEnd 를 부른다. 그때 다른 스레드가 큐의 잠금을 쥐고
        // LavaPlayer 잠금을 기다리고 있어도, onTrackEnd 가 큐의 잠금을 기다리지 않아야 둘 다 멈추지 않는다.
        List<Runnable> handedOff = new ArrayList<>();
        GuildSpeechQueue queue = new GuildSpeechQueue(player, handedOff::add);
        AudioTrack finished = mock(AudioTrack.class);
        AudioTrack next = mock(AudioTrack.class);
        when(player.startTrack(next, true)).thenReturn(false, true);
        queue.enqueue(next);

        Thread lavaPlayerThread;
        synchronized (queue) {
            lavaPlayerThread = Thread.ofPlatform().start(
                    () -> queue.onTrackEnd(player, finished, AudioTrackEndReason.FINISHED));
            lavaPlayerThread.join(2_000);
            assertThat(lavaPlayerThread.isAlive()).isFalse();
        }

        assertThat(handedOff).hasSize(1);
        handedOff.getFirst().run();
        verify(player, times(2)).startTrack(next, true);
        verify(player, never()).startTrack(any(), eq(false));
        verify(player, never()).stopTrack();
    }
}
