package io.github.phs314.quicktts.api;

/**
 * quick chat 요청 본문. 누가 보냈는지는 기기 토큰으로 서버가 판단한다.
 *
 * @param content 읽어 줄 문장. 이름은 디스코드 메시지의 {@code content} 를 따른다
 */
public record QuickChatRequest(String content) {
}
