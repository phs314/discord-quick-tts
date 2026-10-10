package io.github.phs314.quicktts.bot.speech.application.port.in.usecase;

import io.github.phs314.quicktts.bot.speech.domain.vo.GuildId;

/**
 * 봇이 있는 음성 채널에 사람이 아무도 남지 않으면 봇을 내보낸다.
 */
public interface LeaveEmptyVoiceChannelUseCase {

    /** 디스코드 서버에서 누군가 음성 채널을 나갔을 때 부른다. */
    void leaveIfEmpty(GuildId guildId);
}
