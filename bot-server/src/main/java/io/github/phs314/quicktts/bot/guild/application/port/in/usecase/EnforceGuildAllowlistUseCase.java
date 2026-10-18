package io.github.phs314.quicktts.bot.guild.application.port.in.usecase;

import io.github.phs314.quicktts.bot.guild.application.port.in.command.LeaveIfNotAllowedCommand;

/**
 * 봇이 허용 목록에 없는 디스코드 서버에 남아 있지 않게 한다. 허용 목록을 쓰지 않으면 아무것도 하지 않는다.
 */
public interface EnforceGuildAllowlistUseCase {

    /** 봇이 디스코드 서버에 막 초대됐을 때. 허용 목록에 없으면 안내를 남기고 나간다. */
    void leaveIfNotAllowed(LeaveIfNotAllowedCommand command);

    /**
     * 봇 서버가 켜졌을 때 봇이 들어가 있는 모든 디스코드 서버를 확인한다. 꺼져 있는 동안 초대된 곳도 여기서 걸린다.
     * 허용 목록이 비어 있으면(처음 켠 때) 지금 들어가 있는 디스코드 서버를 모두 허용 목록에 넣는다.
     */
    void leaveAllNotAllowed();
}
