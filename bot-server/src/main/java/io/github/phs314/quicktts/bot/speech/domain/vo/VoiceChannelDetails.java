package io.github.phs314.quicktts.bot.speech.domain.vo;

/**
 * 사용자에게 보여 줄 음성 채널 정보.
 *
 * @param channel       음성 채널
 * @param serverName    디스코드 서버 이름
 * @param serverIconUrl 디스코드 서버 아이콘 주소. 아이콘이 없는 서버면 null
 * @param channelName   음성 채널 이름
 */
public record VoiceChannelDetails(VoiceChannel channel, String serverName, String serverIconUrl, String channelName) {
}
