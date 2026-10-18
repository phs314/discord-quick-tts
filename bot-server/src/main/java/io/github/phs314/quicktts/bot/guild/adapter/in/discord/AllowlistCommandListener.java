package io.github.phs314.quicktts.bot.guild.adapter.in.discord;

import io.github.phs314.quicktts.bot.guild.application.exception.NotBotOwnerException;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.AllowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.command.DisallowGuildCommand;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.AllowedGuildDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.dto.GuildAllowlistDto;
import io.github.phs314.quicktts.bot.guild.application.port.in.query.GetAllowlistQuery;
import io.github.phs314.quicktts.bot.guild.application.port.in.usecase.ManageGuildAllowlistUseCase;
import io.github.phs314.quicktts.bot.shared.adapter.in.discord.DiscordSlashCommand;
import io.github.phs314.quicktts.bot.shared.domain.InvalidDomainValueException;
import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.shared.domain.vo.GuildId;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;

/**
 * 디스코드 {@code /허용목록} 명령. 봇 주인이 봇을 들일 디스코드 서버를 넣고 빼고 본다.
 * 디스코드 서버 관리자에게만 보이게 등록하고, 봇 주인인지는 유스케이스가 한 번 더 가린다.
 * 봇과의 개인 메시지에서도 쓸 수 있다.
 */
@Component
@RequiredArgsConstructor
public class AllowlistCommandListener extends ListenerAdapter implements DiscordSlashCommand {

    private static final String COMMAND_NAME = "allowlist";
    private static final String ADD = "add";
    private static final String REMOVE = "remove";
    private static final String LIST = "list";
    private static final String SERVER_OPTION = "server";
    /** 초대 링크에 넣는 권한. README 의 "준비물" 과 같다. */
    private static final long INVITE_PERMISSIONS =
            Permission.getRaw(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK);

    private final JDA jda;
    private final ManageGuildAllowlistUseCase manageGuildAllowlistUseCase;

    @Override
    public SlashCommandData definition() {
        return Commands.slash(COMMAND_NAME, "Manage the Discord servers this bot may stay in (bot owner only)")
                .setNameLocalization(DiscordLocale.KOREAN, "허용목록")
                .setDescriptionLocalization(DiscordLocale.KOREAN, "봇을 들일 디스코드 서버를 관리합니다 (봇 주인 전용)")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .setContexts(InteractionContextType.GUILD, InteractionContextType.BOT_DM)
                .addSubcommands(
                        new SubcommandData(ADD, "Allow a Discord server")
                                .setNameLocalization(DiscordLocale.KOREAN, "추가")
                                .setDescriptionLocalization(DiscordLocale.KOREAN, "디스코드 서버를 허용 목록에 넣습니다")
                                .addOptions(guildOption()),
                        new SubcommandData(REMOVE, "Remove a Discord server; the bot leaves it")
                                .setNameLocalization(DiscordLocale.KOREAN, "삭제")
                                .setDescriptionLocalization(DiscordLocale.KOREAN, "디스코드 서버를 허용 목록에서 빼고, 봇이 그 서버에서 나갑니다")
                                .addOptions(guildOption()),
                        new SubcommandData(LIST, "Show allowed Discord servers")
                                .setNameLocalization(DiscordLocale.KOREAN, "보기")
                                .setDescriptionLocalization(DiscordLocale.KOREAN, "허용 목록을 봅니다"));
    }

    private static OptionData guildOption() {
        // 디스코드 서버 ID 는 디스코드의 정수 옵션 범위(2^53)를 넘을 수 있어 글자로 받는다.
        return new OptionData(OptionType.STRING, SERVER_OPTION, "Discord server ID", true)
                .setNameLocalization(DiscordLocale.KOREAN, "서버")
                .setDescriptionLocalization(DiscordLocale.KOREAN, "디스코드 서버 ID");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!COMMAND_NAME.equals(event.getName())) {
            return;
        }
        DiscordUserId requester = new DiscordUserId(event.getUser().getIdLong());
        String reply;
        try {
            reply = switch (String.valueOf(event.getSubcommandName())) {
                case ADD -> add(requester, GuildId.parse(event.getOption(SERVER_OPTION).getAsString()));
                case REMOVE -> remove(requester, GuildId.parse(event.getOption(SERVER_OPTION).getAsString()));
                default -> list(requester);
            };
        } catch (NotBotOwnerException e) {
            reply = "봇 주인만 쓸 수 있는 명령이에요.";
        } catch (InvalidDomainValueException e) {
            reply = "디스코드 서버 ID 는 숫자예요. 봇이 허용되지 않은 서버에서 나갈 때 남긴 안내에 ID 가 있어요.";
        }
        event.reply(reply).setEphemeral(true).queue();
    }

    private String add(DiscordUserId requester, GuildId guildId) {
        String result = manageGuildAllowlistUseCase.allow(new AllowGuildCommand(requester, guildId))
                ? "허용 목록에 넣었어요: **" + guildName(guildId) + "**"
                : "이미 허용 목록에 있어요: **" + guildName(guildId) + "**";
        if (jda.getGuildById(guildId.value()) != null) {
            return result;
        }
        return result + "\n이 링크로 초대하면 돼요: " + inviteUrl(guildId);
    }

    /** 봇이 그 서버에서 나가는 일은 목록에서 빼는 트랜잭션이 커밋된 뒤에 이벤트로 일어난다 (ADR 0033). */
    private String remove(DiscordUserId requester, GuildId guildId) {
        String name = guildName(guildId);
        return manageGuildAllowlistUseCase.disallow(new DisallowGuildCommand(requester, guildId))
                ? "허용 목록에서 뺐어요: **" + name + "**\n봇이 그 서버에 있었다면 안내를 남기고 나가요."
                : "허용 목록에 없는 서버예요: **" + name + "**";
    }

    private String list(DiscordUserId requester) {
        GuildAllowlistDto allowlist = manageGuildAllowlistUseCase.getAllowlist(new GetAllowlistQuery(requester));
        String header = allowlist.enabled()
                ? "허용 목록을 쓰는 중이에요. 여기 없는 서버에 초대되면 봇이 안내를 남기고 나가요."
                : "허용 목록이 꺼져 있어서 어느 서버에나 들어가요.";
        if (allowlist.guilds().isEmpty()) {
            return header + "\n\n허용한 서버가 없어요.";
        }
        String guilds = allowlist.guilds().stream()
                .map(AllowedGuildDto::guildId)
                .map(guildId -> "・ " + guildName(guildId) + " `" + guildId.value() + "`")
                .collect(Collectors.joining("\n"));
        return header + "\n\n" + guilds;
    }

    private String guildName(GuildId guildId) {
        Guild guild = jda.getGuildById(guildId.value());
        return guild != null ? guild.getName() : "봇이 아직 없는 서버 " + guildId.value();
    }

    /** 그 디스코드 서버가 미리 골라진 봇 초대 링크 */
    private String inviteUrl(GuildId guildId) {
        return "https://discord.com/oauth2/authorize?client_id=" + jda.getSelfUser().getApplicationId()
                + "&scope=bot+applications.commands&permissions=" + INVITE_PERMISSIONS
                + "&guild_id=" + guildId.value() + "&disable_guild_select=true";
    }
}
