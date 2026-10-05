package io.github.phs314.quicktts.common;

/**
 * quick chat 요청 본문.
 *
 * @param discordUserId 문장을 읽어 줄 음성 채널을 찾을 디스코드 사용자 ID
 * @param text          읽어 줄 문장
 */
public record QuickChatRequest(String discordUserId, String text) {
}
