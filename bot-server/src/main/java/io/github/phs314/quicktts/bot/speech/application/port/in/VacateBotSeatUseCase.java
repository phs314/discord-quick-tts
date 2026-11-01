package io.github.phs314.quicktts.bot.speech.application.port.in;

import io.github.phs314.quicktts.bot.speech.application.port.in.command.VacateBotSeatCommand;

/**
 * 봇이 디스코드 서버에서 나가면 그 서버의 봇 자리를 비우고 남은 문장을 버린다.
 */
public interface VacateBotSeatUseCase {

    /** 허용 목록에서 빠지거나 관리자가 내보내서 봇이 디스코드 서버에서 나갔을 때 부른다. */
    void vacate(VacateBotSeatCommand command);
}
