package io.github.phs314.quicktts.bot.guild.application.port.in;

/**
 * 봇이 허용 목록에 없는 디스코드 서버에 남아 있지 않게 한다. 허용 목록을 쓰지 않으면 아무것도 하지 않는다.
 */
public interface LeaveAllNotAllowedGuildsUseCase {

    /**
     * 봇 서버가 켜졌을 때 봇이 들어가 있는 모든 디스코드 서버를 확인한다. 꺼져 있는 동안 초대된 곳도 여기서 걸린다.
     * 허용 목록이 비어 있으면 아직 채우지 않은 것이므로 아무 데서도 나가지 않는다.
     */
    void leaveAllNotAllowed();
}
