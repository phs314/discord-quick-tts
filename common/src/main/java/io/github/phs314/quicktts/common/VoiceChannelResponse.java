package io.github.phs314.quicktts.common;

/**
 * quick chat 을 보내면 읽힐 음성 채널.
 *
 * @param serverName    디스코드 서버 이름
 * @param serverIconUrl 디스코드 서버 아이콘 주소. 아이콘이 없는 서버면 null
 * @param channelName   음성 채널 이름
 */
public record VoiceChannelResponse(String serverName, String serverIconUrl, String channelName) {
}
