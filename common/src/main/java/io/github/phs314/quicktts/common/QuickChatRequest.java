package io.github.phs314.quicktts.common;

/**
 * quick chat 요청 본문. 누가 보냈는지는 기기 토큰으로 서버가 판단한다.
 *
 * @param text 읽어 줄 문장
 */
public record QuickChatRequest(String text) {
}
