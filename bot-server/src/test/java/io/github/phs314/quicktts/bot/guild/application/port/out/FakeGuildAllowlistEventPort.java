package io.github.phs314.quicktts.bot.guild.application.port.out;

import io.github.phs314.quicktts.bot.guild.domain.event.GuildDisallowed;
import java.util.ArrayList;
import java.util.List;

/** 스프링 이벤트 대신 쓰는 대역. 낸 이벤트를 {@link #published()} 에 남긴다. */
public class FakeGuildAllowlistEventPort implements GuildAllowlistEventPort {

    private final List<GuildDisallowed> published = new ArrayList<>();

    @Override
    public void publish(GuildDisallowed event) {
        published.add(event);
    }

    /** 낸 이벤트들, 낸 순서대로. */
    public List<GuildDisallowed> published() {
        return List.copyOf(published);
    }
}
