package io.github.phs314.quicktts.bot.speech.adapter.out.discord;

import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoProvider;
import java.util.List;
import java.util.Objects;

/**
 * 메모리에 있는 음성 바이트를 LavaPlayer 가 읽는 스트림으로 내놓는다. 파일 소스의 스트림과 같은 역할을 한다.
 * 캐시된 음성을 여러 곳이 함께 쓰므로 바이트는 읽기만 한다.
 */
final class ByteArraySeekableInputStream extends SeekableInputStream {

    private final byte[] audio;
    private int position;

    ByteArraySeekableInputStream(byte[] audio) {
        super(audio.length, audio.length);
        this.audio = audio;
    }

    @Override
    public int read() {
        return position < audio.length ? audio[position++] & 0xff : -1;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, buffer.length);
        if (length == 0) {
            return 0;
        }
        if (position >= audio.length) {
            return -1;
        }
        int count = Math.min(length, audio.length - position);
        System.arraycopy(audio, position, buffer, offset, count);
        position += count;
        return count;
    }

    @Override
    public long skip(long count) {
        int skipped = (int) Math.max(0, Math.min(count, audio.length - position));
        position += skipped;
        return skipped;
    }

    @Override
    public int available() {
        return audio.length - position;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    protected void seekHard(long target) {
        position = Math.clamp(target, 0, audio.length);
    }

    @Override
    public boolean canSeekHard() {
        return true;
    }

    @Override
    public List<AudioTrackInfoProvider> getTrackInfoProviders() {
        return List.of();
    }
}
