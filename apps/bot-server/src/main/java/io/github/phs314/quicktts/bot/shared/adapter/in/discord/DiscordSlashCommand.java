package io.github.phs314.quicktts.bot.shared.adapter.in.discord;

import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * 디스코드 슬래시 명령 하나. 구현체를 스프링 빈으로 만들면 시작할 때 명령 등록과 이벤트 구독이 한 번에 된다.
 * 디스코드는 명령 목록을 통째로 바꾸므로 각 명령이 따로 등록하지 않고 한곳에서 모아 등록한다.
 */
public interface DiscordSlashCommand extends EventListener {

    SlashCommandData definition();
}
