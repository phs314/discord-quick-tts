package io.github.phs314.quicktts.bot.speech.adapter.in.web;

import io.github.phs314.quicktts.bot.shared.domain.vo.DiscordUserId;
import io.github.phs314.quicktts.bot.speech.application.port.in.command.SpeakQuickChatCommand;
import io.github.phs314.quicktts.bot.speech.application.port.in.query.FindCurrentUserVoiceChannelQuery;
import io.github.phs314.quicktts.bot.speech.domain.vo.QuickChatMessage;
import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannelDetails;
import io.github.phs314.quicktts.common.QuickChatRequest;
import io.github.phs314.quicktts.common.VoiceChannelResponse;

/**
 * {@link QuickChatController} 의 웹 요청·응답과 유스케이스 입력·결과를 서로 바꾼다 (ADR 0030).
 * 보낸 사람은 요청 본문이 아니라 기기 토큰으로 알아낸 값이라 따로 받는다.
 */
final class QuickChatWebMapper {

    private QuickChatWebMapper() {
    }

    static SpeakQuickChatCommand toCommand(DiscordUserId author, QuickChatRequest request) {
        return new SpeakQuickChatCommand(author, new QuickChatMessage(request.text()));
    }

    static FindCurrentUserVoiceChannelQuery toQuery(DiscordUserId user) {
        return new FindCurrentUserVoiceChannelQuery(user);
    }

    /** 웹 JSON 이름은 배포된 클라이언트가 읽으므로 {@code serverName} 그대로 둔다 (ADR 0027). */
    static VoiceChannelResponse toResponse(VoiceChannelDetails details) {
        return new VoiceChannelResponse(details.guildName(), details.guildIconUrl(), details.channelName());
    }
}
