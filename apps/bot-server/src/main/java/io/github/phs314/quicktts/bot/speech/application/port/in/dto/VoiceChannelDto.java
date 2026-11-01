package io.github.phs314.quicktts.bot.speech.application.port.in.dto;

import io.github.phs314.quicktts.bot.speech.domain.vo.VoiceChannel;

/**
 * 사용자에게 보여 줄 음성 채널 정보. 규칙에 쓰이지 않는 화면용 값이라 도메인이 아니라 조회 결과로 둔다.
 *
 * @param channel      음성 채널
 * @param guildName    디스코드 서버 이름
 * @param guildIconUrl 디스코드 서버 아이콘 주소. 아이콘이 없는 서버면 null
 * @param channelName  음성 채널 이름
 */
public record VoiceChannelDto(VoiceChannel channel, String guildName, String guildIconUrl, String channelName) {
}
