package io.github.phs314.quicktts.bot.guild.application.port.out;

/**
 * 허용 목록을 쓰는지 알려 준다. 누구나 초대할 수 있게 공개할 때 끈다.
 */
public interface GuildAllowlistConfigPort {

    boolean isEnabled();
}
