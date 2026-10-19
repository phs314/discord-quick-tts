package io.github.phs314.quicktts.bot.guild.adapter.in.event;

import io.github.phs314.quicktts.bot.guild.application.port.in.EnforceGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.LeaveIfNotAllowedCommand;
import io.github.phs314.quicktts.bot.guild.domain.event.GuildDisallowed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 디스코드 서버를 허용 목록에서 뺀 트랜잭션이 커밋되면 봇을 그 서버에서 내보낸다.
 * 나가기는 되돌릴 수 없어서 트랜잭션 밖에서 한다 (ADR 0033).
 */
@Component
@RequiredArgsConstructor
public class GuildDisallowedListener {

    private final EnforceGuildAllowlistUseCase enforceGuildAllowlistUseCase;

    @TransactionalEventListener
    void leave(GuildDisallowed event) {
        enforceGuildAllowlistUseCase.leaveIfNotAllowed(new LeaveIfNotAllowedCommand(event.guildId()));
    }
}
