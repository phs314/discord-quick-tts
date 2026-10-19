package io.github.phs314.quicktts.bot.guild.application.port.in;

import io.github.phs314.quicktts.bot.guild.application.port.in.command.LeaveIfNotAllowedCommand;

/**
 * 봇이 허용 목록에 없는 디스코드 서버에 남아 있지 않게 한다. 허용 목록을 쓰지 않으면 아무것도 하지 않는다.
 */
public interface LeaveNotAllowedGuildUseCase {

    /** 봇이 디스코드 서버에 막 초대됐을 때나 허용 목록에서 뺀 뒤. 허용 목록에 없으면 안내를 남기고 나간다. */
    void leaveIfNotAllowed(LeaveIfNotAllowedCommand command);
}
