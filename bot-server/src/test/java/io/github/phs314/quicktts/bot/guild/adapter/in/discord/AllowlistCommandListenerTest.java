package io.github.phs314.quicktts.bot.guild.adapter.in.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.ManageGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.AllowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.DisallowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.AllowedGuildDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.GuildAllowlistDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.query.GetAllowlistQuery;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AllowlistCommandListenerTest {

    private static final DiscordUserId OWNER = new DiscordUserId(42L);
    private static final GuildId JOINED = new GuildId(100L);
    private static final GuildId NOT_JOINED = new GuildId(200L);

    private final JDA jda = mock(JDA.class, RETURNS_DEEP_STUBS);
    private final ManageGuildAllowlistUseCase manageGuildAllowlistUseCase = mock(ManageGuildAllowlistUseCase.class);
    private final AllowlistCommandListener listener = new AllowlistCommandListener(jda, manageGuildAllowlistUseCase);
    private final ReplyCallbackAction reply = mock(ReplyCallbackAction.class, RETURNS_SELF);
    /** 가장 최근에 만든 명령 이벤트. {@link #replied()} 가 여기에 간 답을 꺼낸다. */
    private SlashCommandInteractionEvent event;

    @BeforeEach
    void setUp() {
        Guild joined = mock(Guild.class);
        when(joined.getName()).thenReturn("우리 서버");
        when(jda.getGuildById(JOINED.value())).thenReturn(joined);
        when(jda.getGuildById(NOT_JOINED.value())).thenReturn(null);
        when(jda.getSelfUser().getApplicationId()).thenReturn("777");
    }

    @Test
    void 봇이_있는_서버를_넣으면_이름만_알린다() {
        when(manageGuildAllowlistUseCase.allow(new AllowGuildCommand(OWNER, JOINED))).thenReturn(true);

        listener.onSlashCommandInteraction(command("add", String.valueOf(JOINED.value())));

        assertThat(replied()).isEqualTo("허용 목록에 넣었어요: **우리 서버**");
        verify(reply).setEphemeral(true);
    }

    @Test
    void 봇이_없는_서버를_넣으면_그_서버가_골라진_초대_링크를_준다() {
        when(manageGuildAllowlistUseCase.allow(new AllowGuildCommand(OWNER, NOT_JOINED))).thenReturn(false);

        listener.onSlashCommandInteraction(command("add", " 200 "));

        assertThat(replied())
                .startsWith("이미 허용 목록에 있어요: **봇이 아직 없는 서버 200**\n이 링크로 초대하면 돼요: ")
                .contains("client_id=777", "guild_id=200", "disable_guild_select=true");
    }

    @Test
    void 서버_ID_가_숫자가_아니면_어디서_찾는지_알려_준다() {
        listener.onSlashCommandInteraction(command("add", "우리 서버"));

        assertThat(replied()).startsWith("디스코드 서버 ID 는 숫자예요.");
        verify(manageGuildAllowlistUseCase, never()).allow(any());
    }

    @Test
    void 봇_주인이_아니면_쓸_수_없다고_답한다() {
        when(manageGuildAllowlistUseCase.disallow(new DisallowGuildCommand(OWNER, JOINED)))
                .thenThrow(new NotBotOwnerException(OWNER));

        listener.onSlashCommandInteraction(command("remove", String.valueOf(JOINED.value())));

        assertThat(replied()).isEqualTo("봇 주인만 쓸 수 있는 명령이에요.");
    }

    @Test
    void 목록에_없는_서버를_빼면_없다고_답한다() {
        when(manageGuildAllowlistUseCase.disallow(new DisallowGuildCommand(OWNER, NOT_JOINED))).thenReturn(false);

        listener.onSlashCommandInteraction(command("remove", String.valueOf(NOT_JOINED.value())));

        assertThat(replied()).isEqualTo("허용 목록에 없는 서버예요: **봇이 아직 없는 서버 200**");
    }

    @Test
    void 보기는_켜짐_여부와_서버_이름_ID_를_보여_준다() {
        when(manageGuildAllowlistUseCase.getAllowlist(new GetAllowlistQuery(OWNER))).thenReturn(new GuildAllowlistDto(true,
                List.of(allowed(JOINED), allowed(NOT_JOINED))));

        listener.onSlashCommandInteraction(command("list", null));

        assertThat(replied()).isEqualTo("허용 목록을 쓰는 중이에요. 여기 없는 서버에 초대되면 봇이 안내를 남기고 나가요.\n\n"
                + "・ 우리 서버 `100`\n"
                + "・ 봇이 아직 없는 서버 200 `200`");
    }

    @Test
    void 허용_목록이_꺼져_있고_비어_있으면_그렇게_알린다() {
        when(manageGuildAllowlistUseCase.getAllowlist(new GetAllowlistQuery(OWNER)))
                .thenReturn(new GuildAllowlistDto(false, List.of()));

        listener.onSlashCommandInteraction(command("list", null));

        assertThat(replied()).isEqualTo("허용 목록이 꺼져 있어서 어느 서버에나 들어가요.\n\n허용한 서버가 없어요.");
    }

    private SlashCommandInteractionEvent command(String subcommand, String server) {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, RETURNS_DEEP_STUBS);
        when(event.getName()).thenReturn("allowlist");
        when(event.getSubcommandName()).thenReturn(subcommand);
        when(event.getUser().getIdLong()).thenReturn(OWNER.value());
        if (server != null) {
            when(event.getOption("server").getAsString()).thenReturn(server);
        }
        when(event.reply(anyString())).thenReturn(reply);
        this.event = event;
        return event;
    }

    private String replied() {
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(event).reply(text.capture());
        verify(reply).queue();
        return text.getValue();
    }

    private static AllowedGuildDto allowed(GuildId guildId) {
        return new AllowedGuildDto(guildId, Instant.parse("2026-10-18T00:00:00Z"));
    }
}
