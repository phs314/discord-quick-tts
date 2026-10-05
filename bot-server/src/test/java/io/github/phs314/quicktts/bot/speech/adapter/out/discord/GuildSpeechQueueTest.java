package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.junit.jupiter.api.Test;

class GuildSpeechQueueTest {

    private final AudioPlayer player = mock(AudioPlayer.class);
    private final GuildSpeechQueue queue = new GuildSpeechQueue(player);

    @Test
    void 재생_중이면_기다렸다가_앞_문장이_끝나면_다음_문장을_읽는다() {
        AudioTrack first = mock(AudioTrack.class);
        AudioTrack second = mock(AudioTrack.class);
        when(player.startTrack(first, true)).thenReturn(true);
        when(player.startTrack(second, true)).thenReturn(false);

        queue.enqueue(first);
        queue.enqueue(second);
        queue.onTrackEnd(player, first, AudioTrackEndReason.FINISHED);

        verify(player).startTrack(second, false);
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
        verify(player, never()).startTrack(any(), eq(false));
    }
}
