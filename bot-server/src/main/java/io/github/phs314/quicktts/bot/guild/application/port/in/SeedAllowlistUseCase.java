package io.github.phs314.quicktts.bot.guild.application.port.in;

/**
 * 처음 켠 봇 서버가 이미 들어가 있는 디스코드 서버에서 나가지 않게, 빈 허용 목록을 지금 서버들로 채운다.
 * 허용 목록을 쓰지 않으면 아무것도 하지 않는다.
 */
public interface SeedAllowlistUseCase {

    /**
     * 봇 서버가 켜졌을 때 {@link LeaveAllNotAllowedGuildsUseCase#leaveAllNotAllowed()} 보다 먼저 부른다.
     * 허용 목록을 쓰는데 비어 있으면(처음 켠 때) 지금 들어가 있는 디스코드 서버를 모두 허용 목록에 넣는다.
     * 하나라도 넣지 못하면 하나도 넣지 않는다.
     */
    void seedIfEmpty();
}
